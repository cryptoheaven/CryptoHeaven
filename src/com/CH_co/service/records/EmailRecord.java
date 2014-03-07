/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.records;

import java.sql.Timestamp;
import java.util.*;

import com.CH_co.util.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.23 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class EmailRecord extends Record {

  public static final int MAX_EMAIL_ADDRESSES_DEMO = 1;
  public static final int MAX_EMAIL_ADDRESSES_PERSONAL = 5;
  public static final int MAX_EMAIL_ADDRESSES_BUSINESS = 20;


  public Long emlId;
  public Long userId;
  public Long creatorId;
  public String emailAddr;
  public String personal;
  public Character isHosted; // 'Y'/'N' (default yes, no for web accounts)
  public Timestamp dateCreated;
  public Timestamp dateUpdated;

  public Long getId() {
    return emlId;
  }

  public int getIcon() {
    return ImageNums.EMAIL_SYMBOL_SECURE_SMALL;
  }

  public String getEmailAddressFull() {
    if (personal != null)
      return personal + " <" + emailAddr + ">";
    else
      return emailAddr;
  }

  public String[] getEmailAddressSet() {
    return new String[] { personal, emailAddr };
  }

  public String getNick() {
    return getNick(emailAddr);
  }
  public String getDomain() {
    return getDomain(emailAddr);
  }

  public static String getNick(String emailAddr) {
    String nick = null;
    String[] emls = gatherAddresses(emailAddr);
    if (emls != null && emls.length > 0) {
      String eml = emls[emls.length-1]; // take last email to account for things like "nick@domain.com" <nick@domain.com>
      nick = eml.substring(0, eml.indexOf('@'));
    }
    return nick;
  }
  public static String getDomain(String emailAddr) {
    String domain = null;
    String[] emls = gatherAddresses(emailAddr);
    if (emls != null && emls.length > 0) {
      String eml = emls[emls.length-1]; // take last email to account for things like "nick@domain.com" <nick@domain.com>
      domain = eml.substring(eml.indexOf('@')+1);
    }
    return domain;
  }

  /**
   * Simple address is in form nick@domain without personal part
   */
  private static String getNickFromSimpleAddress(String simpleEmailAddress) {
    return simpleEmailAddress.substring(0, simpleEmailAddress.indexOf('@'));
  }
  private static String getDomainFromSimpleAddress(String simpleEmailAddress) {
    return simpleEmailAddress.substring(simpleEmailAddress.indexOf('@')+1);
  }

  /**
   * @return Personal part of email address ie: "nick@domain.com" returns NULL and "First Last <nick@domain.com>" returns "First Last"
   */
  public static String getPersonal(String fullEmailAddress) {
    String personal = null;
    int braceStart = fullEmailAddress.indexOf('<');
    if (braceStart > 0) {
      personal = fullEmailAddress.substring(0, braceStart).trim();
      if (personal.length() > 2 && personal.startsWith("\"") && personal.endsWith("\""))
        personal = personal.substring(1, personal.length()-1);
    }
    if (personal != null && personal.length() == 0)
      personal = null;
    return personal;
  }

  /**
   * @return Personal part of email address ie: "nick@domain.com" returns "nick" and "First Last <nick@domain.com>" returns "First Last"
   */
  public static String getPersonalOrNick(String fullEmailAddress) {
    String personalOrNick = getPersonal(fullEmailAddress);
    if (personalOrNick == null)
      personalOrNick = getNick(fullEmailAddress);
    return personalOrNick;
  }

  public static String[] getUniqueEmailDomains(EmailRecord[] records) {
    String[] domains = null;
    if (records != null) {
      ArrayList domainsL = new ArrayList();
      HashSet domainsLowerHS = new HashSet();
      for (int i=0; i<records.length; i++) {
        String domain = records[i].getDomain();
        String domainLower = domain != null ? domain.toLowerCase() : null;
        if (domainLower != null && !domainsLowerHS.contains(domainLower)) {
          domainsL.add(domain);
          domainsLowerHS.add(domainLower);
        }
      }
      domains = (String[]) ArrayUtils.toArray(domainsL, String.class);
    }
    return domains;
  }

  public static boolean isAddressEqual(String emailAddr1, String emailAddr2) {
    if (emailAddr1 == null && emailAddr2 == null)
      return true;
    else if (emailAddr1 == null || emailAddr2 == null)
      return false;
    else if (emailAddr1.equals(emailAddr2) || emailAddr1.equalsIgnoreCase(emailAddr2))
      return true;
    else {
      String nick1 = getNick(emailAddr1);
      String domain1 = getDomain(emailAddr1);
      String nick2 = getNick(emailAddr2);
      String domain2 = getDomain(emailAddr2);
      boolean sameNick = (nick1 == null && nick2 == null) || (nick1 != null && nick1.equalsIgnoreCase(nick2));
      boolean sameDomain = (domain1 == null && domain2 == null) || (domain1 != null && domain1.equalsIgnoreCase(domain2));
      return sameNick && sameDomain;
    }
  }

  public static boolean isAddressEqualStrict(String emailAddr1, String emailAddr2) {
    boolean rc = false;
    if (isAddressEqual(emailAddr1, emailAddr2)) {
      String personal1 = getPersonal(emailAddr1);
      String personal2 = getPersonal(emailAddr2);
      rc = (personal1 == null && personal2 == null) || (personal1 != null && personal1.equals(personal2));
    }
    return rc;
  }

  public static boolean isDomainEqual(String emailAddrOrDomain1, String emailAddrOrDomain2) {
    String domain1 = emailAddrOrDomain1.indexOf('@') == -1 ? emailAddrOrDomain1 : getDomain(emailAddrOrDomain1);
    String domain2 = emailAddrOrDomain2.indexOf('@') == -1 ? emailAddrOrDomain2 : getDomain(emailAddrOrDomain2);
    return domain1.equalsIgnoreCase(domain2);
  }

  public boolean isHosted() {
    if (isHosted != null && isHosted.charValue() == 'N')
      return false;
    return true;
  }

  public static int findNickname(String[] nicks, String nickToFind) {
    int index = -1;
    if (nicks != null) {
      for (int i=0; i<nicks.length; i++) {
        if (nicks[i].equalsIgnoreCase(nickToFind)) {
          index = i;
          break;
        }
      }
    }
    return index;
  }
  public static int findDomain(String[] domains, String domainToFind) {
    int index = -1;
    if (domains != null) {
      for (int i=0; i<domains.length; i++) {
        if (domains[i].equalsIgnoreCase(domainToFind)) {
          index = i;
          break;
        }
      }
    }
    return index;
  }

  public static int findEmailAddress(EmailRecord[] emailRecords, String emailAddress) {
    int index = -1;
    if (emailRecords != null) {
      for (int i=0; i<emailRecords.length; i++) {
        if (isAddressEqual(emailRecords[i].emailAddr, emailAddress)) {
          index = i;
          break;
        }
      }
    }
    return index;
  }
  public static int findEmailAddress(List emailAddressesStrsL, String emailAddress) {
    int index = -1;
    if (emailAddressesStrsL != null) {
      for (int i=0; i<emailAddressesStrsL.size(); i++) {
        if (isAddressEqual((String) emailAddressesStrsL.get(i), emailAddress)) {
          index = i;
          break;
        }
      }
    }
    return index;
  }

  public static Long[] getUserIDs(EmailRecord[] records) {
    Long[] IDs = null;
    if (records != null) {
      IDs = new Long[records.length];
      for (int i=0; i<records.length; i++) {
        IDs[i] = records[i].userId;
      }
      IDs = (Long[]) ArrayUtils.removeDuplicates(IDs);
    }
    return IDs;
  }

  public boolean isGiven() {
    return !userId.equals(creatorId);
  }


  /**
   * @return true only if the specified domain is of valid format ie: 'my.domain.com'
   */
  public static boolean isDomainNameValid(String domain) {
    return EmailRecord.isEmailFormatValid("user@"+domain);
  }

  /**
   * @return nickName converted into an email compliant username, ie: no spaces or special characters
   */
  public static String validateDomainName(String domainName) {
    return validateEmailNickOrDomain(domainName, URLs.getElements(URLs.DOMAIN_MAIL)[0]);
  }
  public static String validateNickName(String nickName) {
    return validateEmailNickOrDomain(nickName, "Substitute");
  }
  private static String validateEmailNickOrDomain(String address, String emptySubstitute) {
    // default to substitute nothing and capitalize after substitutions
    return validateEmailNickOrDomain(address, null, emptySubstitute, true);
  }
  /**
   * This function can easily be converted to substitute longer strings instead of just a single character or blanks.
   * @param subC is the substitute character, nullable for blank substitution
   */
  public static String validateEmailNickOrDomain(String address, Character subC, String emptySubstitute, boolean capitalizeAfterSubstitution) {
    StringBuffer sb = new StringBuffer();
    char[] chars = address != null ? address.toCharArray() : "".toCharArray();
    char lastC = subC != null ? subC.charValue() : '-';
    boolean lastSafeSub = false;
    for (int i=0; i<chars.length; i++) {
      char c = chars[i];
      // convert to 7-bit ascii
      c = (char) (0x7F & c);
      if (Character.isLetterOrDigit(c)) {
        if (capitalizeAfterSubstitution && lastSafeSub)
          sb.append(Character.toUpperCase(c));
        else
          sb.append(c);
        lastSafeSub = false;
        lastC = c;
      } else if (i+1 < chars.length) { // if not the last character
        if (lastC=='-' || lastC=='_' || lastC=='.' || (subC!=null && lastC==subC.charValue())) {
        } else {
          if (c=='-' || c=='_' || c=='.' || (subC!=null && c==subC.charValue())) {
            sb.append(c);
            lastSafeSub = false;
          } else {
            if (subC != null)
              sb.append(subC);
            lastSafeSub = true;
          }
          lastC = c;
        }
      }
    }
    if (lastSafeSub) {
      int subStringLength = subC != null ? 1 : 0; // also works for longer strings
      if (subStringLength > 0)
        sb.delete(sb.length() - subStringLength, sb.length());
    }
    if (sb.length() > 0 && (lastC=='-' || lastC=='_' || lastC=='.')) {
      sb.deleteCharAt(sb.length()-1);
    }
    String rcStr = null;
    if (sb.length() == 0) {
      rcStr = emptySubstitute;
    } else {
      rcStr = sb.toString();
    }
    return rcStr;
  }


  /**
   * @return email address converted into an email compliant format, ie: no spaces or special characters
   */
  public static String validateEmailAddress(String emailAddress) {
    String nick = getNick(emailAddress);
    String domain = getDomain(emailAddress);
    domain = validateDomainName(domain);
    nick = validateNickName(nick);
    return nick + '@' + domain;
  }

  /**
   * A little dumb email format checker, only checks few things.
   * Name and domain must start and end with letter or digit.
   * Forbiden character sets list.
   * @param email is an Email Address in the simple form, without any personal part, just nick@domain
   * @return true if email address has a good chance to be valid, false if it certainly is invalid.
   */
  public static boolean isEmailFormatValid(String email) {
    String[] forbidenSets = new String[] { " ","\t","\n" };
    if (email != null && email.length() >= 7) {
      int atIndex = email.indexOf('@');
      if (atIndex >= 1) {
        if (email.indexOf('@', atIndex+1) == -1) {
          int dotIndex = email.lastIndexOf('.');
          if (dotIndex > (atIndex+2)) {
            if (dotIndex < (email.length()-2)) {
              if (email.charAt(email.length()-1) != '.') {
                String domain = EmailRecord.getDomainFromSimpleAddress(email);
                String nick = EmailRecord.getNickFromSimpleAddress(email);
                char[] dChars = domain.toCharArray();
                char[] nChars = nick.toCharArray();

                // check if all characters are 7-bit ASCII
                boolean is7Bit = true;
                for (int i=0; is7Bit && i<dChars.length; i++)
                  is7Bit &= (dChars[i] & 0x7F) == dChars[i];
                for (int i=0; is7Bit && i<nChars.length; i++)
                  is7Bit &= (nChars[i] & 0x7F) == nChars[i];

                if (is7Bit && dChars.length >= 5 && nChars.length >= 1) {
                  // Domain name must start and end with a letter or digit.
                  if (Character.isLetterOrDigit(dChars[0]) && Character.isLetterOrDigit(dChars[dChars.length-1])) {
                    // User name must start and end with a letter or digit.
                    if (Character.isLetterOrDigit(nChars[0]) && Character.isLetterOrDigit(nChars[nChars.length-1])) {
                      boolean isForbidenSetFound = false;
                      for (int i=0; i<forbidenSets.length; i++) {
                        if (email.indexOf(forbidenSets[i]) >= 0) {
                          isForbidenSetFound = true;
                          break;
                        }
                      }
                      if (!isForbidenSetFound) {
                        // cannot contain two consecutive non-letter and non-digit characters
                        boolean twoSymbolsInARowFound = false;
                        char[] chars = email.toCharArray();
                        boolean lastLetterOrDigit = false;
                        for (int i=0; i<chars.length; i++) {
                          boolean isLetterOrDigit = Character.isLetterOrDigit(chars[i]);
                          if (!lastLetterOrDigit && !isLetterOrDigit) {
                            twoSymbolsInARowFound = true;
                            break;
                          }
                          lastLetterOrDigit = isLetterOrDigit;
                        }
                        if (!twoSymbolsInARowFound) {
                          boolean isDomainForbidenCharFound = false;
                          for (int i=0; i<dChars.length; i++) {
                            char ch = dChars[i];
                            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '.') {
                              // ok
                            } else {
                              isDomainForbidenCharFound = true;
                              break;
                            }
                          }
                          if (!isDomainForbidenCharFound) {
                            boolean isNicknameForbidenCharFound = false;
                            for (int i=0; i<nChars.length; i++) {
                              char ch = nChars[i];
                              if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_' || ch == '=' || ch == '.') {
                                // ok
                              } else {
                                isNicknameForbidenCharFound = true;
                                break;
                              }
                            }
                            if (!isNicknameForbidenCharFound)
                              return true;
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }


  /**
   * Gather email addresses from a string line.
   * @return an array of potentially valid email addresses.
   */
  public static String[] gatherAddresses(String fullEmailAddresses) {
    return gatherAddresses(fullEmailAddresses, false);
  }
  public static String[] gatherAddresses(String fullEmailAddresses, boolean includeSoloDomains) {
    String[] addresses = null;

    if (fullEmailAddresses != null && fullEmailAddresses.length() > 0) {
      HashMap uniqueHM = new HashMap();
      ArrayList emlsL = new ArrayList();
      StringTokenizer st = new StringTokenizer(fullEmailAddresses, " ,;:[]{}<>\t\r\n'\"\\/");
      while (st.hasMoreTokens()) {
        String eml = st.nextToken();
        eml = eml.trim();
        if (EmailRecord.isEmailFormatValid(eml) || (includeSoloDomains && EmailRecord.isDomainNameValid(eml))) {
          String normalized = eml.toLowerCase();
          String prevEml = (String) uniqueHM.put(normalized, eml);
          if (prevEml == null) {
            emlsL.add(eml);
          } else if (!prevEml.equals(eml)) {
            // stored before but different case... then replace it with new one...
            // important in case email address is of form "Name@Domain.com" <name@domain.com>
            // we would prefer to get the second one as it is meant to be the address instead of the personal name
            for (int i=0; i<emlsL.size(); i++) {
              String emlAddr = (String) emlsL.get(i);
              if (emlAddr.equalsIgnoreCase(eml)) {
                emlsL.set(i, eml);
                break;
              }
            }
          }
        }
      }
      // Emails gathered, send invitations and insert addresses for future reference.
      if (emlsL.size() > 0) {
        addresses = new String[emlsL.size()];
        emlsL.toArray(addresses);
      }
    }

    return addresses;
  }


  public void merge(Record updated) {
    if (updated instanceof EmailRecord) {
      EmailRecord record = (EmailRecord) updated;
      if (record.emlId        != null) emlId        = record.emlId;
      if (record.userId       != null) userId       = record.userId;
      if (record.creatorId    != null) creatorId    = record.creatorId;
      if (record.emailAddr    != null) emailAddr    = record.emailAddr;
      if (record.personal     != null) personal     = record.personal;
      if (record.dateCreated  != null) dateCreated  = record.dateCreated;
      if (record.dateUpdated  != null) dateUpdated  = record.dateUpdated;
    }
    else
      super.mergeError(updated);
  }

  public String toString() {
    return "[EmailRecord"
      + ": emlId="          + emlId
      + ", userId="         + userId
      + ", creatorId="      + creatorId
      + ", emailAddr="      + emailAddr
      + ", personal="       + personal
      + ", isHosted="       + isHosted
      + ", dateCreated="    + dateCreated
      + ", dateUpdated="    + dateUpdated
      + "]";
  }

  public void setId(Long id) {
    emlId = id;
  }

  public static class AddressComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      if (o1 instanceof EmailRecord && o2 instanceof EmailRecord) {
        String e1 = ((EmailRecord) o1).emailAddr;
        String e2 = ((EmailRecord) o2).emailAddr;
        if (EmailRecord.isAddressEqual(e1, e2))
          return 0;
        else
          return e1.compareToIgnoreCase(e2);
      }
      else if (o1 == null)
        return -1;
      else
        return 1;
    }
  }

}