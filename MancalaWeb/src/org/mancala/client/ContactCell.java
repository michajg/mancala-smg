package org.mancala.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * The Cell used to render a {@link ContactInfo}.
 */
class ContactCell extends AbstractCell<ContactInfo> {

	/**
	 * The html of the image used for contacts.
	 */
	// private final String imageHtml;

	// public ContactCell(ImageResource image) {
	// this.imageHtml =
	// }

	@Override
	public void render(Context context, ContactInfo value, SafeHtmlBuilder sb) {
		// Value can be null, so do a null check..
		if (value == null) {
			return;
		}

		sb.appendHtmlConstant("<table>");

		// Add the contact image.
		sb.appendHtmlConstant("<tr><td rowspan='3'>");
		sb.appendHtmlConstant("<img src='" + value.getPicUrl() + "'>");
		sb.appendHtmlConstant("</td>");

		// Add the name
		sb.appendHtmlConstant("<td>");
		sb.appendEscaped(value.getName());
		sb.appendHtmlConstant("</td></tr><tr><td style='font-size:95%;'>");

		// Add the turn text
		sb.appendEscaped(value.getTurn());
		sb.appendHtmlConstant("</td></tr></table>");
	}
}