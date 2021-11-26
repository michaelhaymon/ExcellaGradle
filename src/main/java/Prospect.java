import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
public class Prospect {
    public static final int MINIMUM_AMOUNT_PER_ROLE = 15000;
    public static final int MINIMUM_POSITIONS = 3;
    String name;
    int contractLengthInMonths;
    int positions;
    BigDecimal bidAmount;
    Collection<String> practiceAreas;
    BigDecimal relativeValuePerMonthPerPosition;
    boolean requiresSecurityClearance;

    public Prospect(String name, int contractLengthInMonths, int positions, BigDecimal bidAmount,
                    Collection<String> practiceAreas, boolean requiresSecurityClearance) {
        this.name = name;
        this.contractLengthInMonths = contractLengthInMonths;
        this.positions = positions;
        this.bidAmount = bidAmount;
        this.practiceAreas = practiceAreas;
        this.requiresSecurityClearance = requiresSecurityClearance;
        this.relativeValuePerMonthPerPosition = calculateRelativeValuePerMonthPerPosition(bidAmount,
                contractLengthInMonths, positions);
    }

    private BigDecimal calculateRelativeValuePerMonthPerPosition(BigDecimal bidAmount,
                                                                int contractLengthInMonths, int positions){
        return bidAmount.divide(BigDecimal.valueOf(contractLengthInMonths)).divide(BigDecimal.valueOf(positions));
    }

    public boolean getRequiresSecurityClearance() {
        return requiresSecurityClearance;
    }
}
