package org.socialbiz.cog;

/**
* Represents the address of a user.
* An interface for using with methods that check and compare addresses.
*
* Two known implementations: AddressListEntry which represents either a single
* address or a role reference, and UserProfile which is a complete profile.
* This interface can be used for routines that do not care which of these
* it is.
*/
public interface UserRef
{
    /**
    * Return the best global unique ID for this address list entry.
    * Usually an email address, but not always.  Could be an openid if there is no user
    * profile associated with the initial address.  Or it could be the name of a role.
    */
    public String getUniversalId();

    /**
    * Check to see if this user has the specified address
    * returns true if there is a match, and false if not.
    */
    public boolean hasAnyId(String testAddr);

    /**
    * This operation checks whether the two user objects have the same
    * global addresses.  To be specific, it check if the
    * 1) global id of one object is any id of the other, AND
    * 2) whether the global ID of the other object is any id of the first.
    *
    * Generally the two objects will either be a UserProfile with a number
    * of addresses, or an AddressListEntry with a single address.
    *
    * This does not test all ids of one object with all ides of the other
    * object.  Does that matter?  It is possible that two different user profiles
    * will both have the same minor id (not the official global id) and they will
    * not be detected as the same, but this sounds likea bug if two different profiles
    * have a common minor ID.
    */
    public boolean equals(UserRef other);

    /**
    * Returns the best public name for the user or address if nothing better available
    */
    public String getName();

    /**
    * Creates appropriate HTML output for this UserRef object
    */
    public void writeLink(AuthRequest ar) throws Exception;

}
