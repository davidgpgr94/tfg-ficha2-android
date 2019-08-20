package uva.inf.davidgo.ficha2.pojos;

import java.util.Date;

public class Record {
    private String _id;
    private String employee;
    private Date entry;
    private Date exit;
    private boolean signed_by_employee;
    private boolean signed_by_admin;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public Date getEntry() {
        return entry;
    }

    public void setEntry(Date entry) {
        this.entry = entry;
    }

    public Date getExit() {
        return exit;
    }

    public void setExit(Date exit) {
        this.exit = exit;
    }

    public boolean isSigned_by_employee() {
        return signed_by_employee;
    }

    public void setSigned_by_employee(boolean signed_by_employee) {
        this.signed_by_employee = signed_by_employee;
    }

    public boolean isSigned_by_admin() {
        return signed_by_admin;
    }

    public void setSigned_by_admin(boolean signed_by_admin) {
        this.signed_by_admin = signed_by_admin;
    }
}
