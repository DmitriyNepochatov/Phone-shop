package ua.com.webservice.service.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.com.webservice.mapper.UserMapper;
import ua.com.webservice.model.check.ClientCheck;
import ua.com.webservice.model.dto.UserOrdersForAdmin;
import ua.com.webservice.model.dto.UserRegistration;
import ua.com.webservice.model.shoppingcart.ShoppingCart;
import ua.com.webservice.model.user.RegisteredUser;
import ua.com.webservice.repository.shoppingcart.ShoppingCartRepository;
import ua.com.webservice.repository.user.UserRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShoppingCartRepository shoppingCartRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository, ShoppingCartRepository shoppingCartRepository) {
        this.userRepository = userRepository;
        this.shoppingCartRepository = shoppingCartRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public boolean save(UserRegistration userRegistration, Date dateOfBirth) {
        if (userRepository.findFirstByEmailAddress(userRegistration.getEmailAddress()).isPresent()) {
            return false;
        }

        userRepository.save(UserMapper.mapUserRegistrationToRegisteredUserCreate(userRegistration, passwordEncoder, dateOfBirth));

        RegisteredUser user = userRepository.findFirstByEmailAddress(userRegistration.getEmailAddress()).get();
        ShoppingCart cart = new ShoppingCart();
        cart.setPhoneInstances(new ArrayList<>());
        cart.setRegisteredUser(user);
        shoppingCartRepository.save(cart);

        LOGGER.info("User {} {} {} {} successfully saved", userRegistration.getLastName(), userRegistration.getFirstName(),
                userRegistration.getMiddleName(), userRegistration.getPhoneNumber());
        return true;
    }

    public void saveChanges(RegisteredUser registeredUser) {
        userRepository.save(registeredUser);
        LOGGER.info("User {} {} {} {} successfully changed information about account", registeredUser.getLastName(), registeredUser.getFirstName(),
                registeredUser.getMiddleName(), registeredUser.getPhoneNumber());
    }

    public Optional<RegisteredUser> findUserByEmailAddress(String emailAddress) {
        return userRepository.findFirstByEmailAddress(emailAddress);
    }

    public ShoppingCart findShoppingCartForUserEmail(String emailAddress) {
        return shoppingCartRepository.findShoppingCartForUserEmail(emailAddress).get();
    }

    public List<UserOrdersForAdmin> getUsersOrdersForAdmin(boolean flag) {
        List<UserOrdersForAdmin> usersOrdersForAdmin = new ArrayList<>();

        for (RegisteredUser registeredUser : userRepository.findAllUsersForAdmin()) {
            List<ClientCheck> checkList = new ArrayList<>();
            registeredUser.getChecks().forEach(check -> {
                if (!check.isClosed() && flag) {
                    checkList.add(check);
                }
                if (check.isClosed() && !flag) {
                    checkList.add(check);
                }
            });

            if (!checkList.isEmpty()) {
                List<AtomicBoolean> checker = new ArrayList<>();
                for (int i = 0; i < checkList.size(); i++) {
                    checker.add(new AtomicBoolean(true));
                }

                for (int i = 0; i < checkList.size(); i++) {
                    if (checkList.get(i).getPhoneInstances().get(0).getClientCheck() == null) {
                        checker.get(i).set(false);
                    }
                }

                for (int i = 0; i < checker.size(); i++) {
                    if (checker.get(i).get()) {
                        usersOrdersForAdmin.add(UserMapper.mapRegisteredUsersToUsersOrdersForAdmin(registeredUser, checkList.get(i)));
                    }
                }
            }
        }

        return usersOrdersForAdmin;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RegisteredUser registeredUser = userRepository.findFirstByEmailAddress(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(registeredUser.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                registeredUser.getEmailAddress(),
                registeredUser.getPassword(),
                roles);
    }

    public int countAllUsers() {
        return userRepository.countAllUsers();
    }

    public List<RegisteredUser> findAllUsersWhichDoesntHaveAnyPurchases() {
        return userRepository.findAllUsersWhichDoesntHaveAnyPurchases();
    }

    public List<RegisteredUser> findAllUsersWhichHaveMoreThanOnePurchases() {
        return userRepository.findAllUsersWhichHaveMoreThanOnePurchases();
    }

    public List<RegisteredUser> findAllUsersForAdmin() {
        return userRepository.findAllUsersForAdmin();
    }

    public RegisteredUser findById(String id) {
        return userRepository.findById(id).get();
    }

    public List<RegisteredUser> findUserByLastName(String lastName) {
        return userRepository.findUserByLastName(lastName);
    }

    public int countUserOrdersForUserId(String userId) {
        return userRepository.countUserOrdersForUserId(userId);
    }
}
