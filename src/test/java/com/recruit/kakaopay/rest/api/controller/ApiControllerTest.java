package com.recruit.kakaopay.rest.api.controller;

import com.recruit.kakaopay.rest.api.entity.Origin;
import com.recruit.kakaopay.rest.api.entity.Split;
import com.recruit.kakaopay.rest.api.service.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
//@EnableAutoConfiguration
//@Transactional
public class ApiControllerTest
{
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SendingService sendSvc;
    @Autowired
    private OriginService originSvc;
    @Autowired
    private SplitService splitSvc;
    @Autowired
    private ReceivingService rcvSvc;
    @Autowired
    private ViewService viewSvc;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    //뿌리기 API Start
    @Test
    @DisplayName("10초 이내에 같은 내용으로 뿌리기 요청이 들어올 경우 중복으로 보고 에러 처리")
    public void test001() throws Exception
    {
        expectedException.expectMessage("same request was sent in 10 seconds");
        String sender = "123";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";
        List<String> receiverList = Arrays.asList(receivers.split(","));

        send(sender, roomId, money, receivers);
        sendSvc.verifyRequest(roomId, sender, Integer.parseInt(money), receiverList);
    }
    
    @Test
    @DisplayName("10초 이후에 같은 내용으로 뿌리기 요청이 들어올 경우 중복이 아닌 것으로 보고 정상 처리")
    public void test002() throws Exception
    {
        String sender = "1234";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";
        List<String> receiverList = Arrays.asList(receivers.split(","));

        sendSvc.verifyRequest(roomId, sender, Integer.parseInt(money), receiverList);
        Thread.sleep(10000);
        sendSvc.verifyRequest(roomId, sender, Integer.parseInt(money), receiverList);
    }

    @Test
    @DisplayName("고유 token 발급. token은 3자리 문자열로 구성되며 예측이 불가능해야 합니다.")
    public void test003() throws Exception
    {
        String token = sendSvc.getNewToken();
        assertNotNull(token);
        assertEquals(token.length(), 3);
    }

    @Test
    @DisplayName("뿌릴 금액을 인원수에 맞게 분배")
    public void test004() throws Exception
    {
        String senderId = "12345";
        String roomId = "vdsv";
        int money = 100000;
        String receivers = "1,2,3,4,5";
        List<String> receiverList = Arrays.asList(receivers.split(","));

        String token = sendSvc.getNewToken();
        Origin origin = originSvc.generateOrigin(token, roomId, senderId, money, receiverList);
        List<Split> splitList = splitSvc.generateSplitList(origin, receiverList.size());
        splitList = sendSvc.distributeMoney(splitList, money);

        assertEquals(splitList.stream().filter(s -> s.getSplitMoney() != 0).count(), receiverList.size());
    }

    @Test
    @DisplayName("뿌리기 API 전체 Test")
    public void test005() throws Exception
    {
        String senderId = "12345";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";

        String token = send(senderId, roomId, money, receivers);

        assertNotNull(token);
    }
    //뿌리기 API End

    //받기 API Start
    @Test
    @DisplayName("Token이 DB에 없는 값이면 예외 처리")
    public void test006() throws Exception
    {
        String token = "weird";
        expectedException.expectMessage("There is no token [" + token + "]");
        rcvSvc.verifyToken(token);
    }

//    @Test
    @DisplayName("뿌린 건은 10분간만 유효합니다. 뿌린지 10분이 지난 요청에 대해서는 받기 실패 응답이 내려가야 합니다.")
    public void test007() throws Exception
    {
        expectedException.expectMessage("token was expired 10 minutes.");
        String senderId = "123456";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";

        String token = send(senderId, roomId, money, receivers);
        Thread.sleep(10 * 60 * 1000);
        rcvSvc.verifyToken(token);
    }

    @Test
    @DisplayName("뿌리기 당 한 사용자는 한번만 받을 수 있습니다. -> 이미 받은 사람이 중복 할당 요청할 경우, 에러 처리")
    public void test008() throws Exception
    {
        expectedException.expectMessage("you already got split money");
        String senderId = "44";
        String receiverId = "3";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";

        String token = send(senderId, roomId, money, receivers);
        receive(token, roomId, receiverId);
        rcvSvc.verifyReceiver(token, roomId, receiverId);
    }

    @Test
    @DisplayName("자신이 뿌리기한 건은 자신이 받을 수 없습니다. -> 자신에게 할당 요청할 경우, 에러 처리")
    public void test009() throws Exception
    {
        expectedException.expectMessage("you are sender, cannot receive");
        String senderId = "45";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";

        String token = send(senderId, roomId, money, receivers);
        rcvSvc.verifyReceiver(token, roomId, senderId);
    }

    @Test
    @DisplayName("뿌리기가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다.")
    public void test010() throws Exception
    {
        expectedException.expectMessage("not same room the money was sent");
        String senderId = "46";
        String receiverId = "3";
        String roomId = "vdsv";
        String anotherRoomId = "ssdd";
        String money = "100000";
        String receivers = "1,2,3,4,5";

        String token = send(senderId, roomId, money, receivers);
        rcvSvc.verifyReceiver(token, anotherRoomId, receiverId);
    }

    @Test
    @DisplayName("token에 해당하는 뿌리기 건 중 아직 누구에게도 할당되지 않은 분배건 하나를 API를 호출한 사용자에게 할당")
    public void test011() throws Exception
    {
        String senderId = "47";
        String receiverId = "3";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";
        List<String> receiverList = Arrays.asList(receivers.split(","));
        String token = send(senderId, roomId, money, receivers);
        Split assignedSplit = rcvSvc.assignReceiver(token, receiverId);

        assertNotNull(assignedSplit.getReceiveTime());
        assertNotNull(assignedSplit.getReceiver());
    }

    @Test
    @DisplayName("받기 API 전체 Test")
    public void test012() throws Exception
    {
        String senderId = "48";
        String receiverId = "3";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";

        String token = send(senderId, roomId, money, receivers);
        int receivedMoney = receive(token, roomId, receiverId);
    }
    //받기 API End

    //조회 API Start
    @Test
    @DisplayName("뿌린 사람 자신만 조회를 할 수 있습니다. 다른사람의 뿌리기건이나 유효하지 않은 token에 대해서는 조회 실패 응답이 내려가야 합니다.")
    public void test013() throws Exception
    {
        String senderId = "49";
        String anotherId = "555";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";

        String token = send(senderId, roomId, money, receivers);
        assertFalse(viewSvc.areYouSender(token, anotherId));

        String weirdToken = "weird";
        expectedException.expectMessage("There is no token [" + weirdToken + "]");
        viewSvc.verifyToken(weirdToken);
    }

//    @Test
    @DisplayName("뿌린 건에 대한 조회는 7일 동안 할 수 있습니다.")
    public void test014() throws Exception
    {
        expectedException.expectMessage("token was expired 7 days.");
        String senderId = "50";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";

        String token = send(senderId, roomId, money, receivers);
        Thread.sleep( 7 * 24 * 60 * 60 * 1000);
        viewSvc.verifyToken(token);
    }

    @Test
    @DisplayName("조회 API 전체 Test")
    public void test015() throws Exception
    {
        String sender = "51";
        String roomId = "vdsv";
        String money = "100000";
        String receivers = "1,2,3,4,5";
        String token = send(sender, roomId, money, receivers);

        receive(token, roomId, "2");
        receive(token, roomId, "3");
        receive(token, roomId, "4");
        view(token, sender);
    }
    //조회 API End

    @DisplayName("뿌릴 금액, 뿌릴 인원을 요청값으로 받아서 고유 token을 발급하고 응답값으로 내려줍니다.")
    public String send(String sender, String roomId, String money, String receiverList) throws Exception
    {
        MockHttpServletRequestBuilder message = post("/send/")
                .header("X-USER-ID", sender)
                .header("X-ROOM-ID", roomId)
                .param("money", money)
                .param("receiverList", receiverList);

        MvcResult result = mockMvc.perform(message)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String token = response.getContentAsString();
        return token;
    }

    @DisplayName("뿌리기 시 발급된 token을 요청값으로 받고 " +
            "token에 해당하는 뿌리기 건 중 아직 누구에게도 할당되지 않은 분배건 하나를 " +
            "API를 호출한 사용자에게 할당하고, 그 금액을 응답값으로 내려줍니다")
    public int receive(String token, String roomId, String receiver) throws Exception
    {
        MockHttpServletRequestBuilder message = put("/receive/")
                .header("X-USER-ID", receiver)
                .header("X-ROOM-ID", roomId)
                .param("token", token);

        MvcResult result = mockMvc.perform(message)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        int receivedMoney = Integer.parseInt(response.getContentAsString());
        return receivedMoney;
    }

    @DisplayName("token을 요청값으로 받고 다음 정보를 응답값으로 내려줍니다." +
            "뿌린 시각, 뿌린 금액, 받기 완료된 금액, 받기 완료된 정보 ([받은 금액, 받은 사용자 아이디] 리스트)")
    public void view(String token, String viewer) throws Exception
    {
        MockHttpServletRequestBuilder message = get("/view/")
                .header("X-USER-ID", viewer)
                .param("token", token);

        mockMvc.perform(message)
                .andDo(print())
                .andExpect(status().isOk())
//                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andReturn();
    }
}