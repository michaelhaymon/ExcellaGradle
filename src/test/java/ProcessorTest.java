import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProcessorTest {

    private Processor processor;

    @Mock
    EmployeeService employeeService;
    @Mock
    AccountManagerService accountManagerService;
    @Mock
    RecruitingService recruitingService;

    Prospect badProspectShortContract;
    Prospect badProspectLowPositions;
    Prospect badProspectBidUnderMinimum;
    Prospect goodProspect;
    Employee goodEmployee;

    @BeforeEach
    void setup() {
        badProspectShortContract = new Prospect().builder()
                .name("badProspectShortContract")
                .contractLengthInMonths(2)
                .positions(8)
                .bidAmount(BigDecimal.valueOf(999999999))
                .relativeValuePerMonthPerPosition(BigDecimal.valueOf(1))
                .build();

        badProspectLowPositions = new Prospect().builder()
                .name("badProspectShortContract")
                .contractLengthInMonths(2)
                .positions(8)
                .bidAmount(BigDecimal.valueOf(999999999))
                .relativeValuePerMonthPerPosition(BigDecimal.valueOf(1))
                .build();

        badProspectBidUnderMinimum = new Prospect().builder()
                .name("badProspectShortContract")
                .contractLengthInMonths(2)
                .positions(8)
                .bidAmount(BigDecimal.valueOf(999999999))
                .relativeValuePerMonthPerPosition(BigDecimal.valueOf(1))
                .build();

        goodProspect = new Prospect().builder()
                .name("goodProspect")
                .contractLengthInMonths(24)
                .positions(3)
                .bidAmount(BigDecimal.valueOf(999999999))
                .relativeValuePerMonthPerPosition(BigDecimal.valueOf(1))
                .practiceAreas(Arrays.asList("Test"))
                .build();

        goodEmployee = new Employee().builder()
                .name("goodEmployee")
                .securityClearance(true)
                .practiceAreas(Arrays.asList("Test"))
                .build();
    }

    @Test
    void processProspects_AllBadProspects_ReturnsEmptyCollection() {
        Collection<Prospect> prospects = new ArrayList<>();
        Collection<Prospect> spyProspects = Mockito.spy(prospects);
        prospects.addAll(Arrays.asList(goodProspect));

        prospects = processor.processProspects(spyProspects);
        assertEquals(1, prospects.size());
    }

    @Test
    void processProspects_GoodContract_ReturnsCollectionWithOneProspect() {
        Collection<Prospect> prospects = new ArrayList<>();
        Collection<Prospect> spyProspects = Mockito.spy(prospects);
        prospects.addAll(Arrays.asList(badProspectShortContract,
                badProspectLowPositions, badProspectBidUnderMinimum));

        prospects = processor.processProspects(spyProspects);
        assertEquals(0, prospects.size());
    }

    @Test
    void staffProspects_FiveEmployeesTwoProspectsThreePositionsEach_EmployeeCollectionSizeTwo() {
        Collection<Prospect> prospects = new ArrayList<>();
        Collection<Prospect> spyProspects = Mockito.spy(prospects);
        prospects.addAll(Arrays.asList(goodProspect, goodProspect));

        when(employeeService).getAllBySecurityClearanceFalse().thenReturn(Arrays.asList(goodEmployee,
                goodEmployee, goodEmployee, goodEmployee, goodEmployee, goodEmployee));

        prospects = processor.processProspects(spyProspects);
        processor.staffProspects(prospects);
        assertEquals(2, prospects.size());
        verify(employeeService).getAllBySecurityClearanceFalse();
        verify(accountManagerService).sendContractToAccountManager();
        verify(recruitingService).sendProspectToRecruiting();
    }
}
