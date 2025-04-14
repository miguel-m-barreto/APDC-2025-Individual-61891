package pt.unl.fct.apdc.assignment.util.data;

import static pt.unl.fct.apdc.assignment.util.StringUtil.normalizeWorksheetStatus;

public class WorkSheetListData {
    public String requesterID;
    public String token;
    public String status; // ex: "ADJUDICADO,EM_CURSO"

    public WorkSheetListData() {}

    public WorkSheetListData(String requesterID, String token, String status) {
        this.requesterID = requesterID;
        this.token = token;
        this.status = status;
    }
}
