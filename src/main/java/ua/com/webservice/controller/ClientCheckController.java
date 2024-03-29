package ua.com.webservice.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Token;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.com.webservice.mapper.OrderMapper;
import ua.com.webservice.model.check.ClientCheck;
import ua.com.webservice.model.dto.CreateOrderParams;
import ua.com.webservice.model.dto.OrderInfoForMail;
import ua.com.webservice.model.user.RegisteredUser;
import ua.com.webservice.service.clientcheck.ClientCheckService;
import ua.com.webservice.service.mailsender.MailSender;
import ua.com.webservice.service.phone.PhoneInstanceService;
import ua.com.webservice.service.stripe.StripeService;
import ua.com.webservice.service.user.UserDetailsServiceImpl;
import ua.com.webservice.util.Util;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/client-check")
public class ClientCheckController {
    private final ClientCheckService clientCheckService;
    private final PhoneInstanceService phoneInstanceService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final MailSender mailSender;
    private final StripeService stripeService;
    private static final String REGEX_FOR_PHONE_NUMBER = "[a-zA-Z]";
    private static final int PHONE_NUMBER_SIZE = 13;
    private static final int CREDIT_CARD_NUMBER_COUNT = 16;
    private final List DELIVERY_TYPE = List.of("Самовивіз з магазину", "Служба доставки", "Кур'єрська доставка");
    private final List PAYMENT_TYPE = List.of("Оплатити карткою на сайті", "Оплата при отриманні");

    public ClientCheckController(ClientCheckService clientCheckService, PhoneInstanceService phoneInstanceService,
                                 UserDetailsServiceImpl userDetailsServiceImpl, MailSender mailSender, StripeService stripeService) {
        this.clientCheckService = clientCheckService;
        this.phoneInstanceService = phoneInstanceService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.mailSender = mailSender;
        this.stripeService = stripeService;
    }

    @GetMapping
    public String getClientCheckById(Model model, @RequestParam(value = "id") String id) {
        ClientCheck clientCheck = clientCheckService.findById(id).get();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.M.yyyy HH:mm:ss", Locale.ENGLISH);

        if (clientCheck.getClosedDate() != null) {
            model.addAttribute("closedDate", formatter.format(clientCheck.getClosedDate()));
        }

        model.addAttribute("created", formatter.format(clientCheck.getCreated()));
        model.addAttribute("clientCheck", clientCheck);
        model.addAttribute("totalPrice", phoneInstanceService.findPriceForClientCheckId(clientCheck.getId()));
        model.addAttribute("registeredUser", userDetailsServiceImpl.findUserByEmailAddress(SecurityContextHolder.getContext().getAuthentication().getName()).get());
        return "checkinfo";
    }

    @PostMapping("/create-order-last-params")
    public String createOrderLastParams(Model model, CreateOrderParams createOrderParams) {
        if (createOrderParams.getDeliveryType().equals("Виберіть тип доставки")) {
            return createOrderError(model, "Ви повинні вибрати тип доставки", createOrderParams.getCheckId());
        }
        if (createOrderParams.getPaymentType().equals("Виберіть тип оплати")) {
            return createOrderError(model, "Ви повинні вибрати тип оплати", createOrderParams.getCheckId());
        }
        if (createOrderParams.getRecipient().isBlank()) {
            return createOrderError(model, "Поле Прізвище та ім'я одержувача порожнє", createOrderParams.getCheckId());
        }
        if (createOrderParams.getRecipientNumberPhone().charAt(0) != '+' || Pattern.compile(REGEX_FOR_PHONE_NUMBER).matcher(createOrderParams.getRecipientNumberPhone()).find()
                || createOrderParams.getRecipientNumberPhone().length() != PHONE_NUMBER_SIZE) {
            return createOrderError(model, "Неправильний номер телефону одержувача. Приклад: +380123456789", createOrderParams.getCheckId());
        }

        if (createOrderParams.getDeliveryType().equals(DELIVERY_TYPE.get(1)) ||
                createOrderParams.getDeliveryType().equals(DELIVERY_TYPE.get(2))) {
            if (createOrderParams.getDeliveryAddress().isBlank()) {
                return createOrderError(model, "Поле адреси доставки порожнє", createOrderParams.getCheckId());
            }
        }

        if (createOrderParams.getPaymentType().equals(PAYMENT_TYPE.get(0))) {
            if (createOrderParams.getNameOnCard().isBlank()) {
                return createOrderError(model, "Поле Ім'я на картці порожнє", createOrderParams.getCheckId());
            }
            if (createOrderParams.getCreditCardNumber().isBlank()) {
                return createOrderError(model, "Поле Номер кредитної картки порожнє", createOrderParams.getCheckId());
            }
            if (Pattern.compile(REGEX_FOR_PHONE_NUMBER).matcher(createOrderParams.getCreditCardNumber()).find() ||
                    createOrderParams.getCreditCardNumber().length() != CREDIT_CARD_NUMBER_COUNT) {
                return createOrderError(model, "Неправильний номер кредитної картки", createOrderParams.getCheckId());
            }
            if (createOrderParams.getExpiration().isBlank()) {
                return createOrderError(model, "Поле Термін дії порожнє", createOrderParams.getCheckId());
            }
            if (Pattern.compile(REGEX_FOR_PHONE_NUMBER).matcher(createOrderParams.getExpiration()).find()) {
                return createOrderError(model, "Неправильний термін придатності", createOrderParams.getCheckId());
            }
            if (createOrderParams.getCvc().isBlank()) {
                return createOrderError(model, "Поле CVC порожнє", createOrderParams.getCheckId());
            }
            if (Pattern.compile(REGEX_FOR_PHONE_NUMBER).matcher(createOrderParams.getCvc()).find() ||
                    createOrderParams.getCvc().length() != 3) {
                return createOrderError(model, "Неправильний CVC", createOrderParams.getCheckId());
            }

            String[] expirationArr = createOrderParams.getExpiration().split("/");
            int expirationYear = Integer.parseInt(expirationArr[1]) + 2000;

            if (Integer.parseInt(expirationArr[0]) <= 0 || Integer.parseInt(expirationArr[0]) > 12) {
                return createOrderError(model, "Неправильний місяць закінчення терміну дії", createOrderParams.getCheckId());
            }
            if (expirationYear > LocalDate.now().getYear() + 40) {
                return createOrderError(model, "Неправильний рік закінчення терміну дії", createOrderParams.getCheckId());
            }
            if (expirationYear < LocalDate.now().getYear() || (expirationYear == LocalDate.now().getYear()
                    && Integer.parseInt(expirationArr[0]) < LocalDate.now().getMonthValue())) {
                return createOrderError(model, "Рік закінчення терміну дії вашої картки недійсний", createOrderParams.getCheckId());
            }

            try {
                Map<String, Object> card = new HashMap<>();
                card.put("number", createOrderParams.getCreditCardNumber());
                card.put("exp_month", Integer.parseInt(expirationArr[0]));
                card.put("exp_year", expirationYear);
                card.put("cvc", createOrderParams.getCvc());
                card.put("name", createOrderParams.getNameOnCard());
                Map<String, Object> params = new HashMap<>();
                params.put("card", card);

                Token token = Token.create(params);
                stripeService.charge(
                        phoneInstanceService.findPriceForClientCheckId(createOrderParams.getCheckId()) * 100,
                        "uah",
                        SecurityContextHolder.getContext().getAuthentication().getName(),
                        token.getId()
                );
            }
            catch (StripeException e) {
                return createOrderError(model, Util.translate(e.getMessage()), createOrderParams.getCheckId());
            }
        }

        ClientCheck clientCheck = OrderMapper.createOrderParamsToClientCheck(clientCheckService.findById(createOrderParams.getCheckId()).get(),
                createOrderParams);
        clientCheckService.save(clientCheck);

        SimpleDateFormat formatter = new SimpleDateFormat("dd.M.yyyy HH:mm:ss", Locale.ENGLISH);
        RegisteredUser registeredUser = userDetailsServiceImpl.findUserByEmailAddress(SecurityContextHolder.getContext().getAuthentication().getName()).get();

        if (createOrderParams.isSendEmail()) {
            mailSender.sendMailPurchaseNotice(registeredUser.getEmailAddress(), "Ваше замовлення " + clientCheck.getId() + " було прийнято",
                    new OrderInfoForMail(clientCheck, formatter.format(clientCheck.getCreated()),
                            phoneInstanceService.findPriceForClientCheckId(clientCheck.getId()), true, registeredUser));
        }

        model.addAttribute("created", formatter.format(clientCheck.getCreated()));
        model.addAttribute("totalPrice", phoneInstanceService.findPriceForClientCheckId(clientCheck.getId()));
        model.addAttribute("clientCheck", clientCheck);
        model.addAttribute("registeredUser", registeredUser);
        return "order";
    }

    private String createOrderError(Model model, String msg, String checkId) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.M.yyyy HH:mm:ss", Locale.ENGLISH);
        ClientCheck clientCheckFromDB = clientCheckService.findById(checkId).get();

        model.addAttribute("created", formatter.format(clientCheckFromDB.getCreated()));
        model.addAttribute("totalPrice", phoneInstanceService.findPriceForClientCheckId(clientCheckFromDB.getId()));
        model.addAttribute("deliveryTypes", DELIVERY_TYPE);
        model.addAttribute("paymentTypes", PAYMENT_TYPE);
        model.addAttribute("msg", msg);
        model.addAttribute("createOrderParams", new CreateOrderParams());
        model.addAttribute("clientCheck", clientCheckFromDB);
        model.addAttribute("registeredUser", userDetailsServiceImpl.findUserByEmailAddress(SecurityContextHolder.getContext().getAuthentication().getName()).get());
        return "setparamsfororder";
    }
}
