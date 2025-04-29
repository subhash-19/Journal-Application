package net.engineeringdigest.journalapplication.corn;

import net.engineeringdigest.journalapplication.schedular.UserSchedular;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserSchedularTest {
    @Autowired
    private UserSchedular userSchedular;

    @Test
    public void testFetchUserAndSendSaMail() {
        userSchedular.fetchUsersAndSaMail();
    }

    @Test
    public void fetchUsersAndSaMailTest() {
        userSchedular.fetchUsersAndSaMail();
    }
}
