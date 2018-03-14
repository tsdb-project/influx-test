package app.service;

import app.common.DBConfiguration;
import app.model.TSData.HeaderMapping;
import app.model.TSData.HeaderReverseMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

import java.util.*;

@SpringBootApplication
public class HeaderLookupServiceDemo implements CommandLineRunner {

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    JdbcTemplate jdbcTemplate = new JdbcTemplate();

    public static void main(String[] args) {
        SpringApplication.run(HeaderLookupServiceDemo.class, args);
    }

    @Override
    public void run(String... args) {

        String userIn;
        Scanner sc = new Scanner(System.in);

        System.out.println("Select type (0: Mapping; Other: Reverse mapping): ");

        switch (sc.nextLine()) {
            case "0":
                String tip = "\n\nInput a header name (q to exit): ";
                System.out.println(tip);

                while (!(userIn = sc.nextLine()).toLowerCase().equals("q")) {

                    String[] headerName = userIn.toUpperCase().split("_");
                    if (headerName.length != 2) {
                        System.out.println("Malformed input!");
                    } else {

                        HeaderMapping m = headerSidToDetail(headerName[0]);

                        if (m == null) {
                            System.out.println("No such SID!");
                        } else {
                            if (m.getCount() < Integer.parseInt(headerName[1])) {
                                System.out.println(String.format("Warning: sub index '%s' does not exist!", headerName[1]));
                            }
                            System.out.print(String.format("'%s' is type: %s. ", headerName[0], m.getType()));
                            if (!m.getElectrode().isEmpty()) {
                                System.out.print(String.format("Its electrode is: %s. ", m.getElectrode()));
                            }
                            if (!m.getNotes().isEmpty()) {
                                System.out.print(String.format("It has a note: %s. ", m.getNotes()));
                            }
                        }
                    }
                    System.out.println(tip);
                }
                break;
            default:
                String tip1 = "\n\nInput a type (q to exit): ";
                System.out.println(tip1);

                while (!(userIn = sc.nextLine()).toLowerCase().equals("q")) {

                    List<HeaderReverseMapping> c = headerTypeToPossibleFullSidList(userIn, null);

                    if (c.size() == 0) {
                        System.out.println("No such type!");
                    } else {
                        System.out.println("For type '" + userIn + "':");
                        c.forEach(rm -> {
                            System.out.print(rm.getType() + ": ");
                            System.out.println(String.join(", ", rm.getColumnHeaders()));
                        });
                        System.out.println(String.join(", ", headerReverseMappingColumns(c)));
                    }

                    System.out.println(tip1);
                }
                break;
        }

        SpringApplication.exit(context);
    }

    /**
     * Get possbile mappings header for further process
     *
     * @param r HeaderReverseMapping List
     * @return header names
     */
    public ArrayList<String> headerReverseMappingColumns(List<HeaderReverseMapping> r) {
        Objects.requireNonNull(r);

        ArrayList<String> tmp = new ArrayList<>();
        r.forEach(hrm -> tmp.addAll(Arrays.asList(hrm.getColumnHeaders())));

        return tmp;
    }

    private List<HeaderReverseMapping> headerTypeToPossibleFullSidList(@Nullable String type, @Nullable String note) {

        if (type == null) type = "";
        else type = "%" + type + "%";

        if (note == null) note = "";
        else note = "%" + note + "%";

        List<HeaderReverseMapping> c = jdbcTemplate.query(
                String.format("SELECT type, SID, MAX(SID_Count) AS B FROM `%s` WHERE `type` LIKE ? OR 'note` LIKE ? GROUP BY SID",
                        DBConfiguration.RelationalData.HEADERMAPING),
                new Object[]{type, note},
                (rs, rowNum) -> new HeaderReverseMapping(
                        rs.getString("type"), rs.getString("SID"),
                        rs.getInt("B"))
        );

        return c;
    }

    private HeaderMapping headerSidToDetail(String sidPrefix) {

        List<HeaderMapping> c = jdbcTemplate.query(
                String.format("SELECT * FROM `%s` WHERE SID = ?", DBConfiguration.RelationalData.HEADERMAPING),
                new Object[]{sidPrefix},
                (rs, rowNum) -> new HeaderMapping(
                        rs.getLong("id"), rs.getString("type"),
                        rs.getString("electrode"),
                        rs.getDouble("freq_low"), rs.getDouble("freq_high"),
                        rs.getString("notes"),
                        rs.getString("SID"), rs.getInt("SID_Count"))
        );

        if (c.size() == 0) {
            return null;
        } else {
            return c.get(0);
        }
    }

}
