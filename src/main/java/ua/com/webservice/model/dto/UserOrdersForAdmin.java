package ua.com.webservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.webservice.model.check.ClientCheck;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserOrdersForAdmin implements Comparable<UserOrdersForAdmin> {
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private ClientCheck check;
    private String dates;
    private String datesClosed;
    private int totalPrices;

    @Override
    public int compareTo(UserOrdersForAdmin o) {
        String thisString = this.lastName+" "+ this.firstName;
        String oString = o.lastName+" "+ o.firstName;
        return thisString.compareTo(oString);
    }
}
