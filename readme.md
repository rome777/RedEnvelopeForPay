# RedEnvelope 핵심 문제해결 전략

##개발 환경
- IDE: JetBrains IntelliJ IDEA 2020.3.2
- Database: Oracle 12c
- Programming Language: Java 1.8
- Application Framework: Spring Boot 2.1.4.RELEASE
- ORM: JPA

---

##DB Table 구조
1.	Origin (Token 단위, 뿌릴 금액 현황)  
      1.1.	Token (PK)  
      1.2.	보낸 시각  
      1.3.	대화방 식별값  
      1.4.	보낸 사람  
      1.5.	전체 금액  
      1.6.	남은 금액  
      1.7.	받을 인원들  
      1.8.	받은 인원들  
      1.9.	마지막 요청 시각  
      1.10.	마지막 요청 인원  
      1.11.	마지막 요청 내용  
2.	Splited (인원수에 맞게 분배된, 뿌릴 금액)  
      2.1.	Splited 고유 번호 (Token뒤에 숫자 붙이기, PK) : 1만 명 이하로 가정하여 뒤에 4자리 붙이기  
      2.2.	Token (Origin ))  
      2.3.	대화방 식별값  
      2.4.	받은 시각  
      2.5.	받은 인원  
      2.6.	분배된 금액  
      2.7.	마지막 요청 시각  
      2.8.	마지막 요청 인원  
      2.9.	마지막 요청 내용  

위 두 Table은 각각의 History Table이 있다. 마지막 ~ 인 Column이 순서 상, 제일 앞에 온다. History Table에서 마지막이란 의미의 단어는 뺀다. 요청 시간과 고유 번호가 Primary Key이다.  

---

##RedEnvelope Poject Source
###RedEnvelope 의미
중국에서는 특별한 날이면 실물로 세뱃돈, 축의금, 보너스, 용돈 등을 주고 받는 특별한 문화가 있다. 이것을 홍빠오(红包)라고 한다. 영어로 Red Envelope이다.


###RedEnvelope Project 구조
- src
    - main
        - java
            - com.recruit.kakaopay.rest.api
                - constant : 상수값 정의
                - controller : Client로부터 Message받는 부분
                - entity : DB Table & Column 구조대로 객체 정의
                - exception : 간단한 사용자 예외 정의
                - repo : DB와 연결하기 위한 JPA Repository
                - service : 실제 Logic 구체적 정의
        - lib
            - ojdbc8-12.2.0.1.jar : JDBC Driver가 자동 Packaging이 안되어 수동 관리
        - resources
            - application.yml : DB, JPA 설정
    - test
        - java  
            - com.recruit.kakaopay.rest.api.controller  
                - ApiControllerTest : 요구사항에 대한 단위 Test 작성 및 Test 완료. 하기 요구사항마다 오른편 괄호 안에 Method 이름으로 연결.

---

## 상세 구현 요건 및 제약사항 검토 및 Test

### 1. 뿌리기 API [test005]
- 뿌릴 금액, 뿌릴 인원을 요청값으로 받습니다.  
  -> 10초 이내에 같은 내용으로 뿌리기 요청이 들어올 경우 중복으로 보고 에러 처리 [test001, test002]  
- 뿌리기 요청건에 대한 고유 token을 발급하고 응답값으로 내려줍니다. [test003]  
  -> DB에 있는 token값과는 중복이 되면 안된다. -> DB에서 Sequence로 관리해야하는지 의문
- 뿌릴 금액을 인원수에 맞게 분배하여 저장합니다. [test004]  
- token은 3 자리 문자열로 구성되며 예측이 불가능해야 합니다. [test003]  

### 2. 받기 API [test012]
- 뿌리기 시 발급된 token을 요청값으로 받습니다. [test006]  
- token에 해당하는 뿌리기 건 중 아직 누구에게도 할당되지 않은 분배건 하나를 API를 호출한 사용자에게 할당하고, 그 금액을 응답값으로 내려줍니다. [test011]  
- 뿌리기 당 한 사용자는 한번만 받을 수 있습니다. [test008]   
  -> 이미 받은 사람이 중복 할당 요청할 경우, 에러 처리 
- 자신이 뿌리기한 건은 자신이 받을 수 없습니다. [test009]  
  -> 자신에게 할당 요청할 경우, 에러 처리
- 뿌리기가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다. [test010]  
- 뿌린 건은 10 분간만 유효합니다. 뿌린지 10 분이 지난 요청에 대해서는 받기 실패 응답이 내려가야 합니다. [test007]  

### 3. 조회 API [test015]
- 뿌리기 시 발급된 token을 요청값으로 받습니다.  
- token에 해당하는 뿌리기 건의 현재 상태를 응답값으로 내려줍니다. 현재 상태는 다음의 정보를 포함합니다.
- 뿌린 시각, 뿌린 금액, 받기 완료된 금액, 받기 완료된 정보 (\[받은 금액, 받은
사용자 아이디\] 리스트)
- 뿌린 사람 자신만 조회를 할 수 있습니다. 다른사람의 뿌리기건이나 유효하지 않은 token에 대해서는 조회 실패 응답이 내려가야 합니다. [test013]  
- 뿌린 건에 대한 조회는 7 일 동안 할 수 있습니다. [test014]  

---

##특이사항
- Test 시, JPA에 의해 DB Schema 자동 생성
- DB Data 변경 이력 기능 존재
    - 이력 확인 및 Tracking 용이
- 간단한 사용자 예외 처리 존재
    - 추후 수정하여 발전
    
---

끝까지 읽어주셔서 감사합니다.