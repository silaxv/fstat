package data.reports;

import java.util.Date;

public interface ReportGenerator {

    public void initReport(Date periodBegin, Date periodEnd) throws Exception;

    public String getReportText();

}
