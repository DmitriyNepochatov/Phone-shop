package ua.com.alevel.model.accessory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import ua.com.alevel.model.phone.PhoneDescription;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OperationSystem {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "operationSystem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PhoneDescription> phoneDescriptions;
}
