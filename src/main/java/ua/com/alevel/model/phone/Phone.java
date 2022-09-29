package ua.com.alevel.model.phone;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import ua.com.alevel.model.accessory.*;
import ua.com.alevel.model.check.ClientCheck;
import ua.com.alevel.model.rating.Rating;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Phone {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "charge_type_id")
    private ChargeType chargeType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "communication_standard_id")
    private CommunicationStandard communicationStandard;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operation_system_id")
    private OperationSystem operationSystem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "processor_id")
    private Processor processor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_screen_id")
    private TypeScreen typeScreen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "check_id")
    private ClientCheck clientCheck;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rating_id")
    private Rating rating;

    @NotBlank
    private String name;

    @NotBlank
    private String series;

    @NotNull
    private float diagonal;

    @NotBlank
    private String displayResolution;

    @NotNull
    private int screenRefreshRate;

    @NotNull
    private int numberOfSimCards;

    @NotNull
    private int amountOfBuiltInMemory;

    @NotNull
    private int amountOfRam;

    @NotNull
    private int numberOfFrontCameras;

    @NotBlank
    private String infoAboutFrontCameras;

    @NotNull
    private int numberOfMainCameras;

    @NotBlank
    private String infoAboutMainCameras;

    @NotNull
    private float weight;

    @NotNull
    private float height;

    @NotNull
    private float width;

    @NotBlank
    private String degreeOfMoistureProtection;

    private boolean isHaveNfc;

    @NotBlank
    private String color;

    @NotNull
    private int guaranteeTimeMonths;

    @NotBlank
    private String countryProducerOfTheProduct;

    @NotNull
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] phoneFrontAndBack;

    @NotNull
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] leftSideAndRightSide;

    @NotNull
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] upSideAndDownSide;

    @NotBlank
    private String imei;
}
