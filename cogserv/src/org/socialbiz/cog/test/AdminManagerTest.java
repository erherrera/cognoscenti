/**
 *
 */
package  org.socialbiz.cog.test;

import org.socialbiz.cog.NGPageIndex;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 */
public class AdminManagerTest extends BaseTest {


    public void testGetPageInfo() {

        //specify the form method and the form action
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/SomnathBook/Test4/admin.htm");
        request.addParameter("pageId", "Test4");

        try {
            NGPageIndex.initIndex();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
