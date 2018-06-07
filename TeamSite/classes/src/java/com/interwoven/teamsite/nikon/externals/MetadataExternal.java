package com.interwoven.teamsite.nikon.externals;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.springframework.web.util.HtmlUtils;

import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.nikon.components.ComponentHelper;

public class MetadataExternal {
    public static final String PAGE_SCOPE_HEAD_INJECTION = "head_injection";

    public Document injectMetadata(final RequestContext context) {
        final ComponentHelper helper = new ComponentHelper();
        //String k = context.getParameterString("key");
        String k = (String)context.getThisComponent().getInitialParameters().get("key");
        if (k == null) {
            k = "property";
        }
        final Document metadataDCR = helper.getLocalisedDCR(context, "metadataDCR", "dcr", null);
        final List<Node> nodes = metadataDCR.getRootElement().selectNodes("//nbv/type/string");
        final StringBuilder builder = new StringBuilder();
        for (final Node node : nodes) {
            final String keyName = node.selectSingleNode("@name").getText();
            final String value = node.getText();
            builder.append("<meta " + k + "=\"" + HtmlUtils.htmlEscape(keyName) + "\" content=\"" + HtmlUtils.htmlEscape(value) + "\"/>");
        }
        if (builder.length() > 0) {
            context.getPageScopeData().put(PAGE_SCOPE_HEAD_INJECTION, builder.toString());
        }
        return metadataDCR;
    }
}
