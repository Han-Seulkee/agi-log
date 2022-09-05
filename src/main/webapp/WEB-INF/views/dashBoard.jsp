<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>아기로그</title>
<script src="/res/js/agiMain.js"></script>
<script src="https://use.fontawesome.com/releases/v6.1.2/js/all.js"></script>
<!-- <link rel="stylesheet" href="/res/css/agiMain.css"> -->
<link rel="stylesheet" href="/res/css/dashBoard.css">

<!-- 카카오 스크립트 -->
<script src="https://developers.kakao.com/sdk/js/kakao.js"></script>
<!-- 네이버 스크립트 -->
<script	src="https://static.nid.naver.com/js/naveridlogin_js_sdk_2.0.2.js" charset="utf-8"></script>
<script>
Kakao.init('2afdabad57ed92e1cc9de5bd4baed321');
function getInfo() {
	let accessArea = document.getElementById("accessArea");
	if ("${accessInfo.type}"!= null&&"${accessInfo.type}"!="") {
		accessArea.innerHTML = "";
		accessArea.innerHTML = "<span> ${accessInfo.suNickName}님 </span>";
		if ("${accessInfo.type}"== "kakao") {
			accessArea.innerHTML +="<span onclick=\"kakaoLogout();\">로그아웃</span>"
		} else if ("${ accessInfo.type }"== "naver") {
			accessArea.innerHTML +="<span onclick=\"naverLogout(); return false;\">로그아웃</span>"
		} else ;
		accessArea.innerHTML += "<span onclick=\"movePage('MoveCompanyLoginPage')\">기업회원</span>";
	}
}
function openPopUp() {
	testPopUp = window
		.open("https://nid.naver.com/nidlogin.logout",
			"_blank",
			"toolbar=yes,scrollbars=yes,resizable=yes,width=1,height=1");
}
function closePopUp() {
	testPopUp.close();
}

function naverLogout() {
	openPopUp();
	setTimeout(function() {
		closePopUp();
	}, 1000);
	logout();
}
function kakaoLogout() {
	if (Kakao.Auth.getAccessToken()) {
		Kakao.API.request({
			url: '/v1/user/unlink',
			success: function(response) {
				logout();
			},
			fail: function(error) {
				console.log(error)
			},
		})
		Kakao.Auth.setAccessToken(undefined)
	}
}

function me(){
    alert("이동하자 해당 감성일기로");
}
</script>
</head>
<body onload="getInfo()">
	<div id="background">
		<div id="top">
			<div id="accessArea">
				<span onclick="movePage('MoveLoginPage')">로그인</span>
                <span onclick="movePage('MoveJoinPage')">회원가입</span>
                <span onclick="movePage('MoveCompanyLoginPage')">기업회원</span>
			</div>
			<div id="logo" onclick="movePage('MoveMainPage')">
				<img src="/res/img/agi_logo.png" alt="images">
			</div>
			<div id="mainMenuArea">
				<ul id="mainMenuList">
					<li class="mainMenu" onclick="movePage('MoveDailyDiaryPage')">육아일기</li>
					<li class="mainMenu" onclick="movePage('MoveMapPage')">지도</li>
					<li class="mainMenu" onclick="movePage('MoveCalendarPage')">캘린더</li>
					<li class="mainMenu" onclick="movePage('MoveBoardPage')">게시판</li>
					<li class="mainMenu" onclick="movePage('MoveMyPage')">마이페이지</li>
				</ul>
			</div>
		</div>
		<div id="middle">
			<div id="rightArea" class="scrollBar">
				                <div class = "main" style="height: 78%;">
                    <div class = "mainLeft" style="width: 75%; height: 100%; float: left;">
                        <!-- 메인 이미지 영역 -->
                    </div>
                    <div class="mainRight" style="width: 25%; height: 100%; float: right; margin: 0 auto;">
                        <div class="todayFeed" style="position:sticky; height: 10%; text-align: center; margin-top: 25%; font-size: 2rem">
                            <!-- 감성일기 좋아요 많은 순으로 띄어진다. 아님 최신순? -->
                            오늘의 피드
                            <div class="photo" style="position:sticky;">
                                <!-- 오늘의 피드 사진 영역 -->
                                <img src="/res/img/photo.png" onclick="me()" class="todayPhoto">
                                <img src="/res/img/photo.png" onclick="me()" class="todayPhoto">
                                <img src="/res/img/photo.png" onclick="me()" class="todayPhoto">
                                <img src="/res/img/photo.png" onclick="me()" class="todayPhoto">
                                <img src="/res/img/photo.png" onclick="me()" class="todayPhoto">
                                <img src="/res/img/photo.png" onclick="me()" class="todayPhoto">
                                <img src="/res/img/photo.png" onclick="me()" class="todayPhoto">
                                <img src="/res/img/photo.png" onclick="me()" class="todayPhoto">
                                <img src="/res/img/photo.png" onclick="me()" class="todayPhoto">                            
                                </div>
                        </div>
                    </div>
                <div style=" display: flex; height: 20%; width: 100%; margin: 2%;">
                <!-- 하단 설명단 -->
                    <div style="width: 33%; position: relative;">
                        <div style="font-size: 11rem; color: lightgrey;">
                            1
                        </div>
                       <div style="position: absolute; width: 74%; font-size: 1.5rem; margin: -5.2rem 2.2rem;">
                            아이와의 소중한 추억을 기록하고 간직하세요
                       </div>
                    </div>
                    <div style="width: 33%; position: relative;">
                        <div style="font-size: 11rem; color: lightgrey;">
                            2
                        </div>
                        <div style="position: absolute; width: 79%; font-size: 1.5rem; margin: -5.2rem 2.2rem;">
                            아이의 건강상태를 체크하고 전문가와 상담할 수 있어요
                       </div>
                    </div>
                     <div style="width: 34%; position: relative;">
                        <div style="font-size: 11rem; color: lightgrey;">
                            3
                        </div>
                        <div style="position: absolute; width: 79%; font-size: 1.5rem; margin: -5.2rem 2.2rem;">
                            내 주변에 병원, 약국 등 육아에 필요한 시설의 위치를 찾아보세요
                       </div>
                    </div>
                </div>
            </div>
			</div>
		</div>
		<div class="modal">
            <div class="modal_body">
				<div class="modal_head">
					<i class="fa-solid fa-xmark closeBtn editBtn"></i><br />
				</div>
				<div class="modal_content"></div>
				<div class="modal_foot"></div>
            </div>
        </div>
	</div>
	<form id="serverForm"></form>
</body>

</html>