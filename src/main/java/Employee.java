import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
public class Employee {

    String name;
    boolean securityClearance;
    Collection<String> practiceAreas;

}
