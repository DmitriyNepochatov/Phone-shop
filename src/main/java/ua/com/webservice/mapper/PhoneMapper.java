package ua.com.webservice.mapper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ua.com.webservice.model.accessory.*;
import ua.com.webservice.model.dto.*;
import ua.com.webservice.model.phone.PhoneDescription;
import ua.com.webservice.model.phone.PhoneInstance;
import ua.com.webservice.repository.phone.PhoneInstanceRepository;
import ua.com.webservice.repository.phone.PhoneInstanceRepositoryCriteria;
import ua.com.webservice.service.brand.BrandService;
import ua.com.webservice.service.chargetype.ChargeTypeService;
import ua.com.webservice.service.communicationstandard.CommunicationStandardService;
import ua.com.webservice.service.country.CountryService;
import ua.com.webservice.service.operationsystem.OperationSystemService;
import ua.com.webservice.service.phone.PhoneInstanceService;
import ua.com.webservice.service.processor.ProcessorService;
import ua.com.webservice.service.typescreen.TypeScreenService;
import ua.com.webservice.util.Util;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class PhoneMapper {
    private PhoneMapper() {
    }

    public static List<PhoneForMainView> mapPhoneToPhoneForMainView(PhoneInstanceRepository phoneInstanceRepository, List<PhoneForMainView> phoneForMainViewList, Pageable pageable) {
        phoneInstanceRepository.findAllForMainView(pageable).forEach(phoneInstance -> addToList(phoneInstance, phoneForMainViewList));

        return phoneForMainViewList;
    }

    public static List<PhoneForMainView> mapPhoneToPhoneForMainViewToFindBySearch(PhoneInstanceRepository phoneInstanceRepository, List<PhoneForMainView> phoneForMainViewList) {
        phoneInstanceRepository.findAllForMainViewForSearch().forEach(phoneInstance -> addToList(phoneInstance, phoneForMainViewList));

        return phoneForMainViewList;
    }

    public static List<PhoneForMainView> mapPhoneToPhoneForMainViewFilter(PhoneInstanceRepositoryCriteria phoneInstanceRepositoryCriteria, List<PhoneForMainView> phoneForMainViewList, String[] params, int needPhones) {
        phoneInstanceRepositoryCriteria.filterPhones(params, needPhones).forEach(phoneInstance -> addToList(phoneInstance, phoneForMainViewList));

        return phoneForMainViewList;
    }

    private static void addToList(PhoneInstance phoneInstance, List<PhoneForMainView> phoneForMainViewList) {
        phoneForMainViewList.add(new PhoneForMainView(phoneInstance.getId(), phoneInstance.getPhone().getPhoneDescription().getBrand().getName(), (float) Math.ceil(((float) phoneInstance.getPhone().getRating().getTotalPoints() / phoneInstance.getPhone().getRating().getNumberOfPoints()) * Math.pow(10, 1)) / (float) Math.pow(10, 1),
                phoneInstance.getPhone().getPhoneDescription().getName(), phoneInstance.getPhone().getPhoneDescription().getSeries(), phoneInstance.getPhone().getAmountOfBuiltInMemory(), phoneInstance.getPhone().getAmountOfRam(),
                phoneInstance.getPhone().getView().getPhoneFrontAndBack(), phoneInstance.getPrice()));
    }

    public static PhoneInstance mapPhoneForMainViewToPhoneInstance(PhoneInstanceRepository phoneInstanceRepository, PhoneForMainView phoneForMainView) {
        return phoneInstanceRepository.findAllInfoAboutPhone(phoneForMainView.getBrand(), phoneForMainView.getName(),
                phoneForMainView.getSeries(), phoneForMainView.getAmountOfBuiltInMemory(), phoneForMainView.getAmountOfRam(), PageRequest.of(0, 1)).get(0);
    }

    public static List<PhoneColors> getColorsForPhone(PhoneInstanceRepository phoneInstanceRepository, PhoneForMainView phoneForMainView, List<PhoneColors> phoneColors) {
        phoneInstanceRepository.findAllColorsForPhone(phoneForMainView.getBrand(), phoneForMainView.getName(),
                phoneForMainView.getSeries(), phoneForMainView.getAmountOfBuiltInMemory(), phoneForMainView.getAmountOfRam()).forEach(phoneInstance ->
                phoneColors.add(new PhoneColors(phoneInstance.getPhone().getView().getColor(), phoneInstance.getPhone().getView().getPhoneFrontAndBack(),
                        phoneInstance.getPhone().getView().getLeftSideAndRightSide(), phoneInstance.getPhone().getView().getUpSideAndDownSide(), false)));

        return phoneColors;
    }

    public static List<PhoneForShoppingCart> mapPhoneInstanceToPhoneForShoppingCart(PhoneInstanceRepository phoneInstanceRepository, String shoppingCartId, List<PhoneForShoppingCart> result) {
        phoneInstanceRepository.findAllPhonesForShoppingCartId(shoppingCartId).forEach(phoneInstance ->
                result.add(new PhoneForShoppingCart(phoneInstance.getId(), phoneInstance.getPhone().getPhoneDescription().getBrand().getName(), phoneInstance.getPhone().getPhoneDescription().getName(), phoneInstance.getPhone().getPhoneDescription().getSeries(),
                        phoneInstance.getPhone().getAmountOfBuiltInMemory(), phoneInstance.getPhone().getAmountOfRam(), phoneInstance.getPhone().getView().getColor(), phoneInstance.getPhone().getView().getPhoneFrontAndBack(), phoneInstance.getPrice())));

        return result;
    }

    public static PhoneDescription mapCreatePhoneDescriptionToPhoneDescription(PhoneDescription phoneDescriptionForDb, CreatePhoneDescription phoneDescription, BrandService brandService, ChargeTypeService chargeTypeService,
                                                                               CommunicationStandardService communicationStandardService, OperationSystemService operationSystemService,
                                                                               ProcessorService processorService, TypeScreenService typeScreenService, CountryService countryService, SimpleDateFormat formatter, boolean createFlag) throws Exception {
        phoneDescriptionForDb.setBrand(brandService.findBrandByName(phoneDescription.getBrand()).get());
        phoneDescriptionForDb.setChargeType(chargeTypeService.findFirstByName(phoneDescription.getChargeType()).get());
        phoneDescriptionForDb.setCommunicationStandard(communicationStandardService.findFirstByName(phoneDescription.getCommunicationStandard()).get());
        phoneDescriptionForDb.setOperationSystem(operationSystemService.findFirstByName(phoneDescription.getOperationSystem()).get());
        phoneDescriptionForDb.setProcessor(processorService.findFirstByName(phoneDescription.getProcessor()).get());
        phoneDescriptionForDb.setTypeScreen(typeScreenService.findFirstByName(phoneDescription.getTypeScreen()).get());
        phoneDescriptionForDb.setCountry(countryService.findCountryByName(phoneDescription.getCountry()).get());
        phoneDescriptionForDb.setName(phoneDescription.getName());
        phoneDescriptionForDb.setSeries(phoneDescription.getSeries());
        phoneDescriptionForDb.setDiagonal(Float.parseFloat(phoneDescription.getDiagonal()));
        phoneDescriptionForDb.setDisplayResolution(phoneDescription.getDisplayResolution());
        phoneDescriptionForDb.setScreenRefreshRate(Integer.parseInt(phoneDescription.getScreenRefreshRate()));
        phoneDescriptionForDb.setNumberOfSimCards(Integer.parseInt(phoneDescription.getNumberOfSimCards()));
        phoneDescriptionForDb.setNumberOfFrontCameras(Integer.parseInt(phoneDescription.getNumberOfFrontCameras()));
        phoneDescriptionForDb.setInfoAboutFrontCameras(phoneDescription.getInfoAboutFrontCameras());
        phoneDescriptionForDb.setNumberOfMainCameras(Integer.parseInt(phoneDescription.getNumberOfMainCameras()));
        phoneDescriptionForDb.setInfoAboutMainCameras(phoneDescription.getInfoAboutMainCameras());
        phoneDescriptionForDb.setWeight(Float.parseFloat(phoneDescription.getWeight()));
        phoneDescriptionForDb.setHeight(Float.parseFloat(phoneDescription.getHeight()));
        phoneDescriptionForDb.setWidth(Float.parseFloat(phoneDescription.getWidth()));
        phoneDescriptionForDb.setDegreeOfMoistureProtection(phoneDescription.getDegreeOfMoistureProtection());
        phoneDescriptionForDb.setHaveNfc(phoneDescription.isNfc());
        phoneDescriptionForDb.setGuaranteeTimeMonths(Integer.parseInt(phoneDescription.getGuaranteeTimeMonths()));

        if (createFlag) {
            phoneDescriptionForDb.setDateAddedToDatabase(new Date());
        }
        else {
            phoneDescriptionForDb.setDateAddedToDatabase(formatter.parse(phoneDescription.getDateAddedToDatabase()));
        }

        return phoneDescriptionForDb;
    }

    public static PhoneForMainView mapParamsToPhoneForMainView(Brand brand, String name, String series,
                                                               int amountOfBuiltInMemory, int amountOfRam) {
        PhoneForMainView phoneForMainView = new PhoneForMainView();
        phoneForMainView.setBrand(brand.getId());
        phoneForMainView.setName(name);
        phoneForMainView.setSeries(series);
        phoneForMainView.setAmountOfBuiltInMemory(amountOfBuiltInMemory);
        phoneForMainView.setAmountOfRam(amountOfRam);

        return phoneForMainView;
    }

    public static List<TablesForFirstStatistic> getListTablesForFirstStatistic(SalesSettingsForSpecificModels salesSettingsForSpecificModels, PhoneInstanceService phoneInstanceService) throws Exception {
        List<List<SalesSettingsForSpecificModelsParams>> salesSettingsForSpecificModelsParamsList = new ArrayList<>();

        for (int year = LocalDate.now().getYear(); year != LocalDate.now().getYear() - 3; year--) {
            salesSettingsForSpecificModels.setYear(year + "");

            if (year == LocalDate.now().getYear()) {
                List<SalesSettingsForSpecificModelsParams> salesSettingsForSpecificModelsParams = phoneInstanceService.getSalesSettingsForSpecificModelsParams(salesSettingsForSpecificModels);

                salesSettingsForSpecificModelsParams.forEach(salesSettingsForSpecificModelsParam -> {
                    int monthNumber = salesSettingsForSpecificModelsParam.getFields().size() + 1;

                    for (; monthNumber < 13; monthNumber++) {
                        salesSettingsForSpecificModelsParam.getFields().add(new SalesSettingsForSpecificModelsMonths(Util.getMonth(monthNumber), -1));
                    }
                });

                salesSettingsForSpecificModelsParamsList.add(salesSettingsForSpecificModelsParams);
            }
            else {
                salesSettingsForSpecificModelsParamsList.add(phoneInstanceService.getSalesSettingsForSpecificModelsParams(salesSettingsForSpecificModels));
            }
        }

        List<TablesForFirstStatistic> tablesForFirstStatistics = new ArrayList<>();

        for (SalesSettingsForSpecificModelsParams settings : salesSettingsForSpecificModelsParamsList.get(0)) {
            tablesForFirstStatistics.add(new TablesForFirstStatistic(settings.getPhone(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        }

        for (List<SalesSettingsForSpecificModelsParams> salesSettingsForSpecificModelsParams : salesSettingsForSpecificModelsParamsList) {
            for (SalesSettingsForSpecificModelsParams salesSettingsForSpecificModelsParam : salesSettingsForSpecificModelsParams) {

                for (TablesForFirstStatistic tablesForFirstStatistic : tablesForFirstStatistics) {
                    if (tablesForFirstStatistic.getPhone().equals(salesSettingsForSpecificModelsParam.getPhone()) &&
                            tablesForFirstStatistic.getPhone().getView().getColor().equals(salesSettingsForSpecificModelsParam.getPhone().getView().getColor())) {
                        if (tablesForFirstStatistic.getTempYear().isEmpty()) {
                            tablesForFirstStatistic.setTempYear(salesSettingsForSpecificModelsParam.getFields());
                            break;
                        }
                        if (tablesForFirstStatistic.getLastYear().isEmpty()) {
                            tablesForFirstStatistic.setLastYear(salesSettingsForSpecificModelsParam.getFields());
                            break;
                        }
                        if (tablesForFirstStatistic.getYearBeforeLast().isEmpty()) {
                            tablesForFirstStatistic.setYearBeforeLast(salesSettingsForSpecificModelsParam.getFields());
                            break;
                        }
                    }
                }
            }
        }

        return tablesForFirstStatistics;
    }
}
