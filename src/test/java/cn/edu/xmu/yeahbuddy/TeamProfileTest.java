package cn.edu.xmu.yeahbuddy;

import cn.edu.xmu.yeahbuddy.domain.Team;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Rollback
@TestExecutionListeners(listeners = {WithSecurityContextTestExecutionListener.class})
public class TeamProfileTest extends ApplicationTestBase {

    @Autowired
    private MessageSource messageSource;

    @Test
    @Transactional
    @WithUserDetails(value = "testteam", userDetailsServiceBeanName = "teamService")
    public void teamProfileTest() throws Exception {
        Optional<Team> testteam = teamService.findByUsername("testteam");

        if (!testteam.isPresent()) throw new RuntimeException();

        mvc.perform(get(String.format("/team/%d", testteam.get().getId())).accept(MediaType.TEXT_HTML))
           .andExpect(status().isOk())
           .andExpect(view().name("team/profile"));

        mvc.perform(put(String.format("/team/%d?locale=en&displayName=test TEAM", testteam.get().getId()))
                            //workarounds https://jira.spring.io/browse/SPR-15753
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value(messageSource.getMessage("response.ok", new Object[]{}, Locale.ENGLISH)))
           .andExpect(jsonPath("$.error").doesNotExist());

        Assert.assertEquals("test TEAM", testteam.get().getDisplayName());

        mvc.perform(put(String.format("/team/%d?locale=en&username=test2team", testteam.get().getId()))
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .accept(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isConflict());
    }
}
