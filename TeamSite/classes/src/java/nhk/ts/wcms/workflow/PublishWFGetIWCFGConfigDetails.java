package nhk.ts.wcms.workflow;

import com.interwoven.serverutils100.IWConfig;

public class PublishWFGetIWCFGConfigDetails {
    public static String getHost() {
        return PublishWFGetIWCFGConfigDetails.cfg.getString("iwwebd", "host", "sgdev-base");
    }

    public static String getMailPassword() {
        return PublishWFGetIWCFGConfigDetails.cfg.getString("iwsend_mail", "mailpasswd", "cHCqS0mY3PIb");
    }

    public static String getMailServer() {
        return PublishWFGetIWCFGConfigDetails.cfg.getString("iwsend_mail", "mailserver", "smtpdm.aliyun.com");
    }

    public static String getMailUser() {
        return PublishWFGetIWCFGConfigDetails.cfg.getString("iwsend_mail", "mailuser", "noreply@nikonalert.kddicloud.com.cn");
    }

    protected static final IWConfig cfg = IWConfig.getConfig();
}
