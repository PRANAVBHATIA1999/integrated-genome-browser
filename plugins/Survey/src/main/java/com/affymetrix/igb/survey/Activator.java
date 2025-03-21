package com.affymetrix.igb.survey;

import static com.affymetrix.common.CommonUtils.isDevelopmentMode;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.common.PreferenceUtils;
import com.affymetrix.genoviz.swing.AMenuItem;
import static com.affymetrix.igb.survey.ShowSurvey.showSurvey;
import com.affymetrix.igb.swing.JRPMenu;
import com.affymetrix.igb.swing.JRPMenuItem;
import com.google.common.io.Resources;
import org.lorainelab.igb.services.IgbService;
import org.lorainelab.igb.services.XServiceRegistrar;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import javax.swing.JMenuItem;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hiralv
 */
public class Activator extends XServiceRegistrar<IgbService> implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    public Activator() {
        super(IgbService.class);
    }

    @Override
    protected ServiceRegistration<?>[] getServices(BundleContext bundleContext, IgbService igbService) throws Exception {
        ResourceBundle BUNDLE = ResourceBundle.getBundle("survey");
        JRPMenu surveysMenu = new JRPMenu("Survey_surveysMenu", "News and Surveys");

        InputStream inputStream = null;
        try {
//            inputStream = Activator.class.getClassLoader().getResourceAsStream("surveys.xml");
            URL url = new URL(BUNDLE.getString("surveys"));
            byte repsonseXMLBytes[] = Resources.toByteArray(url);
            String repsonseXML = new String(repsonseXMLBytes, "UTF-8");
            
            if(repsonseXML.toLowerCase().contains("html")){
                String newURL = repsonseXML.split("href=\"")[1].split("\"")[0];
                inputStream = new ByteArrayInputStream(Resources.toByteArray(new URL(newURL)));
            }
            else {
                inputStream = new ByteArrayInputStream(repsonseXMLBytes);
            }
            
            if (inputStream != null) {
                List<Survey> surveys = SurveyParser.parse(inputStream);
                GeneralUtils.safeClose(inputStream);

                Collections.sort(surveys, new Comparator<Survey>() {
                    public int compare(Survey o1, Survey o2) {
                        return o1.getEnd().compareTo(o2.getEnd());
                    }
                });

                Date today = Calendar.getInstance().getTime();
                final Predicate<Survey> isExpiredSurvey = survey -> today.compareTo(survey.getStart()) >= 0 && today.compareTo(survey.getEnd()) < 0;

                surveys.stream().filter(isExpiredSurvey).forEach(survey -> {
                    JMenuItem item = new JRPMenuItem(survey.getName(),
                            new GenericAction(survey.getName(), null, null) {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    super.actionPerformed(e);
                                    GeneralUtils.browse(survey.getLink());
                                }
                            }
                    );
                    surveysMenu.add(item);
                });
                for (final Survey survey : surveys) {
                    if (today.compareTo(survey.getStart()) >= 0
                            && today.compareTo(survey.getEnd()) < 0
                            && !PreferenceUtils.getBooleanParam(survey.getId(), false)) {

                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                //to prevent constant popups during development
                                if (!isDevelopmentMode()) {
                                    showSurvey(survey);
                                }
                            }
                        }, 10000);

                        break;
                    }
                }
            }

            if (surveysMenu.getItemCount() > 0) {
                return new ServiceRegistration<?>[]{bundleContext.registerService(AMenuItem.class, new AMenuItem(surveysMenu, "help"), null)};
            }
        } catch (FileNotFoundException ex) {
            logger.info("There are currently no surveys available.");
        } catch (IOException ex) {
            logger.info("Error reading survey.xml");
        }

        return null;
    }
}
