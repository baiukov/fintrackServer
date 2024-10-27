package me.vse.fintrackserver.mappers;

import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.enums.Frequencies;
import me.vse.fintrackserver.model.Category;
import me.vse.fintrackserver.model.StandingOrder;
import me.vse.fintrackserver.model.dto.CategoryDto;
import me.vse.fintrackserver.rest.requests.TransactionRequest;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes = FintrackServerApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StandingOrderMapperTest {

    @Autowired
    private StandingOrderMapper standingOrderMapper;

    private Stream<Arguments> getMapperScenarios() {
        return Stream.of(
                Arguments.of(
                        TransactionRequest.builder().frequency(Frequencies.DAILY).build(),
                        StandingOrder.builder().id("standOrdId").build(),
                        StandingOrder.builder().id("standOrdId").frequency(Frequencies.DAILY).build()
                ),
                Arguments.of(
                        TransactionRequest.builder().build(),
                        StandingOrder.builder().id("standOrdId").frequency(Frequencies.DAILY).build(),
                        StandingOrder.builder().id("standOrdId").frequency(Frequencies.DAILY).build()
                ),
                Arguments.of(
                        TransactionRequest.builder().frequency(Frequencies.WEEKLY).build(),
                        StandingOrder.builder().id("standOrdId").frequency(Frequencies.DAILY).build(),
                        StandingOrder.builder().id("standOrdId").frequency(Frequencies.WEEKLY).build()
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .frequency(Frequencies.DAILY)
                                .remindDaysBefore(3)
                                .build(),
                        StandingOrder.builder().id("standOrdId").build(),
                        StandingOrder.builder().id("standOrdId")
                                .frequency(Frequencies.DAILY)
                                .remindDaysBefore(3)
                                .build()
                ),
                Arguments.of(
                        TransactionRequest.builder()
                                .frequency(Frequencies.MONTHLY)
                                .remindDaysBefore(10)
                                .build(),
                        StandingOrder.builder().id("standOrdId")
                                .frequency(Frequencies.DAILY)
                                .remindDaysBefore(3)
                                .build(),
                        StandingOrder.builder().id("standOrdId")
                                .frequency(Frequencies.MONTHLY)
                                .remindDaysBefore(10)
                                .build()
                )
        );
    }


    @ParameterizedTest(name = "Standing order mapper test. Given request: {0}, old standing order: {1}. " +
            "Should return standing order {2}")
    @MethodSource("getMapperScenarios")
    public void updateStandingOrderFromRequestTest(TransactionRequest request, StandingOrder oldStandingOrder,
                                       StandingOrder newStandingOrder) {
        standingOrderMapper.updateStandingOrderFromRequest(request, oldStandingOrder);
        assertEquals(newStandingOrder, oldStandingOrder);
    }
}
