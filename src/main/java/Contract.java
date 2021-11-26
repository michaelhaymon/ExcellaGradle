import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
public class Contract {
    private String name;
    private boolean federal = false;
    BigDecimal bidAmount;
    int contractLength;
    int positions;
    Collection<String> practiceAreas;
    Collection<Employee> employees;

    public Contract() {

    }
}
