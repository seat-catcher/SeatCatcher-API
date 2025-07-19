package com.sullung2yo.seatcatcher.common.rabbitmq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
@Slf4j
@SpringBootTest
public class RabbitMQTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private String exchangeName = "seatcatcher-exchange";

    BlockingQueue<PathHistoryResponse.PathHistoryInfoResponse> blockingQueue = new LinkedBlockingDeque<>();

    @RabbitListener(queues = "seatcatcher-path-queue")
    public void handlePathHistoryEvent(PathHistoryResponse.PathHistoryInfoResponse response) {
        boolean isSuccess = blockingQueue.offer(response);
        if(!isSuccess)
        {
            log.warn("PublishEventTest :: Failed to enqueue websocket message in test!");
        }
    }

    @Test
    void convertAndSend_shouldActuallyReceiveMessages() throws Exception
    {
        // given
        PathHistoryResponse.PathHistoryInfoResponse mockData = new PathHistoryResponse.PathHistoryInfoResponse();
        mockData.setId(1L);

        // when
        rabbitTemplate.convertAndSend(exchangeName, "path-histories." + mockData.getId(), mockData);
        PathHistoryResponse.PathHistoryInfoResponse received = blockingQueue.poll(5, TimeUnit.SECONDS);

        // then
        assertNotNull(received);
        assertEquals(mockData.getId(), received.getId());
    }
}
 */
