package nhk.ts.wcms.workflow;

import com.interwoven.serverutils100.IWConfig;

public class PublishWFGetIWCFGConfigDetails{
	protected static final IWConfig cfg = IWConfig.getConfig();
	public static String getMailServer() {
		return cfg.getString("iwsend_mail", "mailserver", "exbehq02.Interwoven.com");
	}

	public static String getHost() {
		return cfg.getString("iwwebd", "host", "sgdev-base");
	}
}