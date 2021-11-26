import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/*
    In this example, Excella has a public facing website that allows
    prospective clients to enter the requirements for contracts they
    wish to arrange with Excella. The data for each contract includes
    client name, length of contract in months, number of desired
    positions filled, bid amount for total contract, and the languages
    needed for the client's tech stack. This info is stored into a
    database and processed by a batch job routinely. The following is
    a detailed pseudocode representation of how that batch job might
    function and provide utility to Excella. This is a purely a theoretical
    example to indicate my capabilities and train of thought while coding,
    not a representation of any sort of actual intended application. Some
    access modifiers were chosen for simplicity, not security.
 */

public class Processor {

    @Autowired
    private final ProspectService prospectService;
    @Autowired
    private final EmployeeService employeeService;
    @Autowired
    private final AccountManagerService accountManagerService;
    @Autowired
    private final RecruitingService recruitingService;

    private static final int MINIMUM_CONTRACT_LENGTH_IN_MONTHS = 6;

    public static void main(String[] args) {
        //  prospectService connects to repository class of client information received
        //  from database.
        Collection<Prospect> prospects = prospectService.getAllProspects();
        prospects = processProspects(prospects);
        staffProspects(prospects);
    }

    static Collection<Prospect> processProspects(Collection<Prospect> prospects) {
        //  This series of filters removes prospects not deemed valuable enough for consideration
        return prospects.stream()
                //  Remove prospects for contracts shorter than 6 months.
                .filter(p -> p.contractLengthInMonths > MINIMUM_CONTRACT_LENGTH_IN_MONTHS)
                //  Remove prospects for teams with less than 3 positions.
                .filter(p -> p.positions >= Prospect.MINIMUM_POSITIONS)
                //  Remove prospects for contracts whose bid / positions
                //  would result in paying developers less than federal
                //  minimum wage.
                .filter(p -> isBidOverMinimumPerPosition(p.relativeValuePerMonthPerPosition))
                //  Sort by contract length, then relative value per month per position, then
                //  by lowest number of positions required to fulfill contract.
                .sorted(Comparator
                        .comparing(Prospect::getContractLengthInMonths).reversed()
                        .thenComparing(Prospect::getRelativeValuePerMonthPerPosition).reversed()
                        .thenComparing(Prospect::getPositions)
                )
                .collect(Collectors.toList());
    }

    static void staffProspects(Collection<Prospect> prospects) {
        //  employeeService connects to repository interface of Excella employees
        //  and extends JpaRepository for CRUD functionality.
        Collection<Employee> federalEmployees = employeeService.getAllBySecurityClearanceTrue();
        Collection<Employee> employees = employeeService.getAllBySecurityClearanceFalse();

        for (Prospect prospect : prospects) {
            if (prospect.getRequiresSecurityClearance()) {
                int prospectPositions = prospect.getPositions();
                Collection<Employee> qualifiedEmployees = new ArrayList<>();
                if (federalEmployees.size() > prospectPositions) {
                    while (prospectPositions > 0) {
                        for (Employee federalEmployee : federalEmployees) {
                            if (federalEmployee.getPracticeAreas().containsAll(prospect.getPracticeAreas())) {
                                qualifiedEmployees.add(federalEmployee);
                                prospectPositions--;
                            }
                        }
                    }
                    federalEmployees.removeAll(qualifiedEmployees);
                    buildAndSendContract(prospect, qualifiedEmployees);
                }

                // Sends prospect information to someone who can begin looking for candidates.
                recruitingService.sendProspectToRecruiting(prospect);
            } else {
                int prospectPositions = prospect.getPositions();
                Collection<Employee> qualifiedEmployees = new ArrayList<>();
                if (employees.size() > prospectPositions) {
                    while (prospectPositions > 0) {
                        for (Employee employee : employees) {
                            if (employee.getPracticeAreas().containsAll(prospect.getPracticeAreas())) {
                                qualifiedEmployees.add(employee);
                                prospectPositions--;
                            }
                        }
                    }
                    employees.removeAll(qualifiedEmployees);
                    buildAndSendContract(prospect, qualifiedEmployees);
                }
                // Sends prospect information to someone who can begin looking for candidates.
                recruitingService.sendProspectToRecruiting(prospect);
            }
        }
    }

    public static void buildAndSendContract(Prospect prospect, Collection<Employee> qualifiedEmployees) {
        Contract contract;
        contract = new Contract().builder()
                .name(prospect.getName())
                .federal(prospect.getRequiresSecurityClearance())
                .bidAmount(prospect.getBidAmount())
                .contractLength(prospect.getContractLengthInMonths())
                .positions(qualifiedEmployees.size())
                .practiceAreas(prospect.getPracticeAreas())
                .employees(qualifiedEmployees)
                .build();

        //  Sends contract information to someone who can begin setting up the team.
        accountManagerService.sendContractToAccountManager(contract);
    }

    private static boolean isBidOverMinimumPerPosition(BigDecimal relativeValuePerMonthPerPosition) {
        //  Returns if bid amount is over minimum wage per position required.
        return (relativeValuePerMonthPerPosition.multiply(BigDecimal.valueOf(12)))
                .compareTo(BigDecimal.valueOf(Prospect.MINIMUM_AMOUNT_PER_ROLE)) > 1;
    }

}
