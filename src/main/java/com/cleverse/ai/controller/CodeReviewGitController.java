package com.cleverse.ai.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//OutputStream을 StringBuilder로 리디렉션하기 위한 클래스
class InMemoryDiffOutputStream extends java.io.OutputStream {
private final StringBuilder builder;

public InMemoryDiffOutputStream(StringBuilder builder) {
   this.builder = builder;
}

@Override
public void write(int b) throws IOException {
   builder.append((char) b);
}

@Override
public void write(byte[] b, int off, int len) throws IOException {
   builder.append(new String(b, off, len));
}
}

@Controller
@RequestMapping(value={"/api/codereview/git"})
public class CodeReviewGitController {
	// Git diff를 시작하는 메소드
	@RequestMapping("/gitDiff")
    public String gitDiff() {
        // 원격 저장소 URL, 로컬 저장소 경로, 비교 시작 커밋 ID를 인자로 전달
        String remoteRepoUrl = "https://github.com/juldae719/AICodeReview.git";
        File localRepoDir = new File("F:\\project\\ai\\CleverseAICodeReview\\CleverseAICodeReview");
        String baseCommitId = "ec86441";
        StringBuilder result = new StringBuilder();
        try {
            // 클론 또는 pull 처리
            //handleCloneOrPull(remoteRepoUrl, localRepoDir, result);

            // Git 저장소를 열고 baseCommitId 이후의 커밋들 가져오기
            List<RevCommit> commitList = getCommitList(localRepoDir, baseCommitId);

            // 두 개 이상의 커밋이 없으면 예외 던지기
            if (commitList.size() < 2) {
                throw new IllegalArgumentException("❗ 두 개 이상의 커밋이 있어야 비교할 수 있습니다.");
            }

            // 각 커밋에 대해 diff 비교
            compareCommits(localRepoDir, commitList, result);

        } catch (IllegalArgumentException e) {
            result.append(e.getMessage()).append("\n");
        } catch (Exception e) {
            e.printStackTrace();
            result.append("에러 발생: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }

    // 로컬 저장소가 없으면 클론하고, 있으면 pull
    public void handleCloneOrPull(String remoteRepoUrl, File localRepoDir, StringBuilder result) throws GitAPIException, IOException {
        if (!localRepoDir.exists()) {
            result.append("로컬 저장소가 존재하지 않으므로 클론을 시작합니다...\n");
            Git.cloneRepository()
                    .setURI(remoteRepoUrl)
                    .setDirectory(localRepoDir)
                    .call();
            result.append("클론 완료!\n");
        } else {
            result.append("로컬 저장소가 존재하므로 pull을 시작합니다...\n");
            try (Git git = Git.open(localRepoDir)) {
                git.pull().call();
                result.append("pull 완료!\n");
            }
        }
    }

    // baseCommitId 이후 커밋들을 가져오는 메소드
    public List<RevCommit> getCommitList(File localRepoDir, String baseCommitId) throws IOException, GitAPIException {
        try (Git git = Git.open(localRepoDir)) {
            Repository repository = git.getRepository();
            Iterable<RevCommit> commits = git.log()
                    .addRange(repository.resolve(baseCommitId), repository.resolve("HEAD"))
                    .call();
            
            List<RevCommit> commitList = new ArrayList<>();
            commits.forEach(commitList::add);
            return commitList;
        }
    }

    public String systemUserMessage (final SimpleOpenAI openAI, final String model, final String systemMessage, final String userMessage) {
//      final long startTime = System.currentTimeMillis();
      final ChatRequest chatRequest = ChatRequest.builder()
          .model(model)
          .message(SystemMessage.of(systemMessage))
          .message(UserMessage.of(userMessage))
          .temperature(0.0)
          .maxCompletionTokens(-1)
          .build();
      final CompletableFuture<Chat> futureChat = openAI.chatCompletions().create(chatRequest);
      final Chat chatResponse = futureChat.join();
//      final long stopTime = System.currentTimeMillis();
//      log.info(model + " userMessageOnly took " +  (stopTime - startTime) + " ms");
      return chatResponse.firstContent();
  }
    
    // 커밋들에 대해 diff를 비교하는 메소드
    public void compareCommits(File localRepoDir, List<RevCommit> commitList, StringBuilder result) throws IOException, GitAPIException {
    	final SimpleOpenAI openAI = SimpleOpenAI.builder()
                .apiKey("ollama")
                .organizationId("ollama")
                .projectId("ollama")
                .baseUrl("http://localhost:11434")
                .build();
        try (Git git = Git.open(localRepoDir)) {
            Repository repository = git.getRepository();

            // 각 커밋에 대해 diff 비교
            for (int i = 0; i < commitList.size() - 1; i++) {
                RevCommit current = commitList.get(i);
                RevCommit previous = commitList.get(i + 1);

                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();

                try (ObjectReader reader = repository.newObjectReader()) {
                    oldTreeIter.reset(reader, previous.getTree().getId());
                    newTreeIter.reset(reader, current.getTree().getId());
                }

                List<DiffEntry> diffs = git.diff()
                        .setOldTree(oldTreeIter)
                        .setNewTree(newTreeIter)
                        .call();

                DiffFormatter formatter = new DiffFormatter(new InMemoryDiffOutputStream(result)); // 출력하지 않음
                formatter.setRepository(repository);

                for (DiffEntry entry : diffs) {
                    // 현재 커밋에서 파일 내용 읽기
                    ObjectId newId = entry.getNewId().toObjectId();
                    ObjectLoader loader = repository.open(newId);
                    String content = new String(loader.getBytes());
                    List<String> lines = Arrays.asList(content.split("\\r?\\n"));

                    // 변경된 라인 계산
                    FileHeader fileHeader = formatter.toFileHeader(entry);
                    EditList edits = fileHeader.toEditList();
                    Set<Integer> addedLines = new HashSet<>();
                    Set<Integer> deletedLines = new HashSet<>();

                    for (Edit edit : edits) {
                        if (edit.getType() == Edit.Type.INSERT) {
                            for (int addedLine = edit.getBeginB(); addedLine < edit.getEndB(); addedLine++) {
                                addedLines.add(addedLine); // 추가된 라인
                            }
                        } else if (edit.getType() == Edit.Type.DELETE) {
                            for (int deletedLine = edit.getBeginA(); deletedLine < edit.getEndA(); deletedLine++) {
                                deletedLines.add(deletedLine); // 삭제된 라인
                            }
                        }
                    }

                    // 변경된 라인에 `+` 또는 `-` 표시
                    for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
                        String marker = "";
                        if (addedLines.contains(lineNumber)) {
                            marker = "[+]"; // 추가된 라인
                        } else if (deletedLines.contains(lineNumber)) {
                            marker = "[-]"; // 삭제된 라인
                        }
                        result.append(String.format("%4d: %s %s\n", lineNumber + 1, marker, lines.get(lineNumber)));
                    }
                    String QWEN25CODER = "qwen2.5-coder";
                    String QWEN25CODER32 = "qwen2.5-Coder:32b";
                    String FALCON3 = "falcon3";
                    String DEEPSEEKR1 = "deepseek-r1";
                    String CODELLAMA = "codellama";
                      List<String> codingModels = new ArrayList<>();
//                    codingModels.add(QWEN25CODER);
//                    codingModels.add(FALCON3);
//                    codingModels.add(gemma2);
//                    codingModels.add(DEEPSEEKR1);
                      codingModels.add(QWEN25CODER32);

                        String contents = result.toString();
                        result.setLength(0);
                        System.out.println(contents);
                        String codeReviewSystemMessage = "한국어로 답변해주세요. 응답 형식에 맞게 답변 주세요.";
                        String codeReviewUserMessagePrefix = String.format(""
                				+ " 당신은 숙련된 Java, Javascript, Python 소프트웨어 엔지니어입니다. "
                				+ "  "
                				+ " 지정된 JSON 형식으로만 응답하세요. "
                				+ "  "
                				+ " 중요 지침: 반드시 아래 구조의 JSON 배열로만 응답하세요. 이외의 부가적인 말과 '''json 등은 포함하지 마세요."
                				+ "  "
                				+ " 응답 형식: "
                				+ " [ {{ \"new_line\": int, \"comment\": \"review comment\" }}, ... ] "
                				+ " 리뷰 범위: 주석은 제거하고 '[+]'로 시작하는 라인에 대해서만 리뷰를 작성하세요. '[+]'가 없다면 []로 응답해 주세요"
                				+ " 변수 / 클래스 등이 실제로 정의되어 있는지 여부는 주어진 코드에 명시되지 않은 경우 판단하지 마세요. "
                				+ " 모든 응답은 한국어로 작성해야 합니다. "
                				+ " 입력 데이터: "
                				+ "  "
//                				+ " 이슈의 description: {issue_description} "
//                				+ " MR의 description: 제목: {mr_title} 설명: {mr_description} "
//                				+ " Reference Docs: {docs} "
                				+ " 코드 변경 사항: %s "
//                				+ " 주의: 위 예시는 참고용이며, 응답은 반드시 주어진 코드와 조건에 맞게 생성해야 합니다. "
                				, contents);;

                        Map<String,String> responses = new HashMap<>();
                        for (String model : codingModels) {
                            String response = systemUserMessage(openAI, model, codeReviewSystemMessage, codeReviewUserMessagePrefix );
                            System.out.println(response);
                        }
                }
            }
        }
    }

}
