$(document).ready(function(){

	var $image = $(".image-crop > img")
	$($image).cropper({
		aspectRatio: 1.618,
		preview: ".img-preview",
		done: function(data) {
			// Output the result data for cropping image.
		}
	});

	var $inputImage = $("#inputImage");
	if (window.FileReader) {
		$inputImage.change(function() {
			var fileReader = new FileReader(),
					files = this.files,
					file;

			if (!files.length) {
				return;
			}

			file = files[0];

			if (/^image\/\w+$/.test(file.type)) {
				fileReader.readAsDataURL(file);
				fileReader.onload = function () {
					$inputImage.val("");
					$image.cropper("reset", true).cropper("replace", this.result);
				};
			} else {
				showMessage("Please choose an image file.");
			}
		});
	} else {
		$inputImage.addClass("hide");
	}

	$("#download").click(function() {
		window.open($image.cropper("getDataURL"));
	});

	$("#zoomIn").click(function() {
		$image.cropper("zoom", 0.1);
	});

	$("#zoomOut").click(function() {
		$image.cropper("zoom", -0.1);
	});

	$("#rotateLeft").click(function() {
		$image.cropper("rotate", 45);
	});

	$("#rotateRight").click(function() {
		$image.cropper("rotate", -45);
	});

	$("#setDrag").click(function() {
		$image.cropper("setDragMode", "crop");
	});

	$('#data_1 .input-group.date').datepicker({
		todayBtn: "linked",
		keyboardNavigation: false,
		forceParse: false,
		calendarWeeks: true,
		autoclose: true
	});

	$('#data_2 .input-group.date').datepicker({
		startView: 1,
		todayBtn: "linked",
		keyboardNavigation: false,
		forceParse: false,
		autoclose: true,
		format: "dd/mm/yyyy"
	});

	$('#data_3 .input-group.date').datepicker({
		startView: 2,
		todayBtn: "linked",
		keyboardNavigation: false,
		forceParse: false,
		autoclose: true
	});

	$('#data_4 .input-group.date').datepicker({
		minViewMode: 1,
		keyboardNavigation: false,
		forceParse: false,
		autoclose: true,
		todayHighlight: true
	});

	$('#data_5 .input-daterange').datepicker({
		keyboardNavigation: false,
		forceParse: false,
		autoclose: true
	});

	$('.i-checks').iCheck({
		checkboxClass: 'icheckbox_square-green',
		radioClass: 'iradio_square-green'
	});

	$('input[name="daterange"]').daterangepicker();

	$('#reportrange span').html(moment().subtract(29, 'days').format('MMMM D, YYYY') + ' - ' + moment().format('MMMM D, YYYY'));

	$('#reportrange').daterangepicker({
		format: 'MM/DD/YYYY',
		startDate: moment().subtract(29, 'days'),
		endDate: moment(),
		minDate: '01/01/2012',
		maxDate: '12/31/2015',
		dateLimit: { days: 60 },
		showDropdowns: true,
		showWeekNumbers: true,
		timePicker: false,
		timePickerIncrement: 1,
		timePicker12Hour: true,
		ranges: {
			'Today': [moment(), moment()],
			'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
			'Last 7 Days': [moment().subtract(6, 'days'), moment()],
			'Last 30 Days': [moment().subtract(29, 'days'), moment()],
			'This Month': [moment().startOf('month'), moment().endOf('month')],
			'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
		},
		opens: 'right',
		drops: 'down',
		buttonClasses: ['btn', 'btn-sm'],
		applyClass: 'btn-primary',
		cancelClass: 'btn-default',
		separator: ' to ',
		locale: {
			applyLabel: 'Submit',
			cancelLabel: 'Cancel',
			fromLabel: 'From',
			toLabel: 'To',
			customRangeLabel: 'Custom',
			daysOfWeek: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr','Sa'],
			monthNames: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
			firstDay: 1
		}
	}, function(start, end, label) {
		console.log(start.toISOString(), end.toISOString(), label);
		$('#reportrange span').html(start.format('MMMM D, YYYY') + ' - ' + end.format('MMMM D, YYYY'));
	});

	$(".select2_demo_1").select2();
	$(".select2_demo_2").select2();
	$(".select2_demo_3").select2({
		placeholder: "Select a state",
		allowClear: true
	});


	$(".touchspin1").TouchSpin({
		buttondown_class: 'btn btn-white',
		buttonup_class: 'btn btn-white'
	});

	$(".touchspin2").TouchSpin({
		min: 0,
		max: 100,
		step: 0.1,
		decimals: 2,
		boostat: 5,
		maxboostedstep: 10,
		postfix: '%',
		buttondown_class: 'btn btn-white',
		buttonup_class: 'btn btn-white'
	});

	$(".touchspin3").TouchSpin({
		verticalbuttons: true,
		buttondown_class: 'btn btn-white',
		buttonup_class: 'btn btn-white'
	});
	
	
	//dataStep1 파일필드 커스텀			
	var fileTarget = $('.filebox .upload-hidden'); 
			
	fileTarget.on('change', function(){ // 값이 변경되면 
	
		if(window.FileReader){ // modern browser 
			var filename = $(this)[0].files[0].name; } 
		
		else { // old IE 
			var filename = $(this).val().split('/').pop().split('\\').pop(); // 파일명만 추출 
		} 
	
		// 추출한 파일명 삽입 
		$(this).siblings('.upload-name').val(filename); 
	});
	//dataStep1 end
	
	
	//dataStep2, dataStep4 상세설정 열고 닫기	
	var resultH = $('.resultset .resultset-wrap').height();
	$('.detailset .detailset-wrap').removeClass('on',80);
	$('.resultset .resultset-wrap').css({'height':resultH +'px'});
	
	$(".bt-open").click(function() {
		
		$('.detailset .detailset-wrap, .detailset .bt-close').addClass('on',80);
		$('.detailset .bt-open').removeClass('on',80);
		
		$('.resultset .resultset-wrap').css({'height':resultH +'px'});
		$('.resultset .bt-close').addClass('on',80);
		$('.resultset .bt-open').removeClass('on',80);
		
	});
	$(".bt-close").click(function() {
		
		$('.detailset .detailset-wrap, .detailset .bt-close').removeClass('on',80);	
		$('.detailset .bt-open').addClass('on',80);			
		
		$('.resultset .resultset-wrap').css({'transition':'1s', 'height':'0'});
		$('.resultset .bt-close').removeClass('on',80);	
		$('.resultset .bt-open').addClass('on',80);
		
	});
	
	//데이터 전체보기 테이블 보기 height 설정
	$('.modal-body .dataprev-wrap').css('height', ($(window).height() - 300) +'px' );
	
	//dataStep2, dataStep4 end


});


var config = {
		'.chosen-select'           : {},
		'.chosen-select-deselect'  : {allow_single_deselect:true},
		'.chosen-select-no-single' : {disable_search_threshold:10},
		'.chosen-select-no-results': {no_results_text:'Oops, nothing found!'},
		'.chosen-select-width'     : {width:"95%"}
		}
	for (var selector in config) {
		$(selector).chosen(config[selector]);
	}

$(".dial").knob();


// scroll
$(window).scroll(function(){   
    guide_lefton();	
});

//userGuide2 마우스 스크롤& class="on" 설정
function fnMove(seq){
	var offset = $("#" + seq).offset();
	$('html, body').animate({scrollTop : offset.top - 150 +"px"}, 800);			
}		
$('.nav.metismenu li a').click(function(){
	$(this).parents('.nav.metismenu').find('li').removeClass('on');
	$(this).parent('li').addClass('on');
	guide_lefton();
});


// guide left on
function guide_lefton(){
	
    if($('.lefton').length){		
		
		var scrollT = $(this).scrollTop();		
		var viewT = $(window).height()/2;
		var step1T = $("#sec1").offset().top - viewT;	//-220
		var step2T = $("#sec2").offset().top - viewT;	//1294
		var step3T = $("#sec3").offset().top - viewT;	//4171;
		var step4T = $("#sec4").offset().top - viewT;
		var step5T = $("#sec5").offset().top - viewT;
		//alert(step3T +"> "+ scrollT +">"+ step2T);
		$('.nav.metismenu').find('li').removeClass('on');
		
		
		if(scrollT < step2T){
			$('.nav.metismenu li:nth-child(1)').addClass('on');
		}
		if(scrollT > step2T){
			if(step3T >= scrollT){
				$('.nav.metismenu li:nth-child(2)').addClass('on');
			}			
		}
		if(scrollT > step3T){
			if(step4T >= scrollT){
				$('.nav.metismenu li:nth-child(3)').addClass('on');
			}
		}
		if(scrollT > step4T){
			if(step5T >= scrollT){
				$('.nav.metismenu li:nth-child(4)').addClass('on');
			}
		}
		if(scrollT > step5T){		
			$('.nav.metismenu li:nth-child(5)').addClass('on');
		}
	}
}