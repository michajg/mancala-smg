package org.mancala.client;

import com.google.gwt.view.client.ProvidesKey;

/**
 * Information about a contact.
 */
public class ContactInfo implements Comparable<ContactInfo> {

	/**
	 * The key provider that provides the unique ID of a contact.
	 */
	public static final ProvidesKey<ContactInfo> KEY_PROVIDER = new ProvidesKey<ContactInfo>() {
		@Override
		public Object getKey(ContactInfo item) {
			return item == null ? null : item.getId();
		}
	};

	private static int nextId = 0;

	private String id;
	private String name;
	private String picUrl;
	private String turn;
	private Long matchId;

	ContactInfo(String id, String name, String picUrl, String turn, Long matchId) {
		this.id = id;
		this.name = name;
		this.picUrl = picUrl;
		this.turn = turn;
		this.matchId = matchId;
	}

	ContactInfo(String id, String name, String picUrl) {
		this(id, name, picUrl, "", null);
	}

	/**
	 * @return the matchId
	 */
	public Long getMatchId() {
		return matchId;
	}

	/**
	 * @param matchId
	 *          the matchId to set
	 */
	public void setMatchId(Long matchId) {
		this.matchId = matchId;
	}

	/**
	 * @return the picUrl
	 */
	public String getPicUrl() {
		return picUrl;
	}

	/**
	 * @param picUrl
	 *          the picUrl to set
	 */
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	@Override
	public int compareTo(ContactInfo o) {
		return (o == null || o.name == null) ? -1 : -o.name.compareTo(name);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the turn
	 */
	public String getTurn() {
		return turn;
	}

	/**
	 * @param turn
	 *          the turn to set
	 */
	public void setTurn(String turn) {
		this.turn = turn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContactInfo other = (ContactInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}

	ContactInfo copy() {
		return new ContactInfo(this.id, this.name, this.picUrl, this.turn, this.matchId);
	}
}
