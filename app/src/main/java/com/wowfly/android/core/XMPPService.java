package com.wowfly.android.core;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.provider.ContactsContract;
import android.util.Log;

import com.wowfly.wowyun.wowyun_mobile.R;

/**
 * Created by user on 8/14/14.
 */
public class XMPPService {
    private static final String TAG = "XMPPService";
    private  ConnectionConfiguration mConfig;
    private  Connection mConn;
    private  boolean bLogin = false;
    private  boolean bBuddyData = false;
    private  Map<String, ChatManager> mChatMap;
    private  Collection<RosterEntry> mRECollection;
    private  List<Map<String, Object>> mBuddyDataList;
    private RosterListener rosterListener;

    public static class BuddyInfo {
        String name;
        String jid;
        boolean isAvailable;
    }
    private String mMyJid = "null";

    private ArrayList<BuddyInfo> mBuddyInfo;

    public XMPPService() {
        mBuddyDataList = new ArrayList<Map<String, Object>>();
        mBuddyInfo = new ArrayList<BuddyInfo>(16);

/*        rosterListener = new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> strings) {

            }

            @Override
            public void entriesUpdated(Collection<String> strings) {

            }

            @Override
            public void entriesDeleted(Collection<String> strings) {

            }

            @Override
            public void presenceChanged(Presence presence) {

            }
        };*/

        try {
            mConfig = new ConnectionConfiguration("wuruxu.com", 5222);
            mConfig.setReconnectionAllowed(true);
            mConfig.setSendPresence(true);
            mConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            mConfig.setCompressionEnabled(false);
            mConfig.setSelfSignedCertificateEnabled(false);
            mConfig.setSASLAuthenticationEnabled(false);
            mConfig.setVerifyChainEnabled(false);
            mConfig.setRosterLoadedAtLogin(true);
            ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp", new VCardProvider());
            mConn = new XMPPConnection(mConfig);
            mConn.connect();
            mChatMap = new HashMap<String, ChatManager>();
            mConn.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
            mConn.addPacketListener(new PacketListener() {
                @Override
                public void processPacket(Packet packet) {
                    if(packet instanceof Presence) {
                        Log.i(TAG, " Presence packet received");
                        Presence presence = (Presence) packet;
                        Log.i(TAG, " xml = " + presence.toXML());
                    }
                }
            }, new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {
                    if(packet instanceof Presence) {
                        Presence presence = (Presence) packet;
                        Log.i(TAG, " PacketFilter Presence accept " + presence.getFrom());
                        //Log.i(TAG, " res = " + presence.getFrom().lastIndexOf('/'));

                        if(presence.getType().equals(Presence.Type.subscribed)) {
                            return true;
                        }

                        if(presence.getType().equals(Presence.Type.subscribe)) {
                            Presence subscribed = new Presence(Presence.Type.subscribed);
                            subscribed.setTo(presence.getFrom());
                            mConn.sendPacket(subscribed);

/*                            subscribed = new Presence(Presence.Type.subscribe);
                            subscribed.setTo(presence.getFrom());
                            mConn.sendPacket(subscribed);*/
                            return true;
                        }
                        if(presence.getType().equals(Presence.Type.unsubscribe))
                            return true;
                        if(presence.getType().equals(Presence.Type.unsubscribed))
                            return true;
                    }
                    return false;
                }
            });
        } catch (XMPPException e) {
            Log.e(TAG, "XMPPService e = " + e.getMessage());
        }
    }

    public int getBuddyCount() {
        return mBuddyDataList.size();
    }

    public BuddyInfo getBuddyInfo(int pos) {
        if(mBuddyInfo.size() > 0 && pos < mBuddyInfo.size()) {
            return mBuddyInfo.get(pos);
        } else {
            return null;
        }
    }

    public void getBuddyList() {
        Roster r = mConn.getRoster();

        //r.reload();
        mRECollection = r.getEntries();

        mBuddyDataList.clear();
        mBuddyInfo.clear();

        for(RosterEntry entry: mRECollection) {
            Log.i(TAG, "name = " + entry.getName() + " user = " + entry.getUser());
            BuddyInfo info = new BuddyInfo();
            info.name = entry.getName();
            info.jid = entry.getUser();
            info.isAvailable = r.getPresence(info.jid).isAvailable();
            if(info.name == null) {
/*                VCard card = new VCard();
                try {
                    card.load(mConn, info.jid);
                    //Log.i(TAG, "nickname " + card.getNickName() + " xml: " + card.toXML());
                    if (card.getNickName() != null)
                        info.name = card.getNickName();
                } catch (XMPPException e) {

                }*/
            }
            mBuddyInfo.add(info);

            Map<String, Object> item = new HashMap<String, Object>();
            item.put("jid", info.jid);

            if(info.name == null) {
                int sep = info.jid.indexOf('@');
                info.name = info.jid.substring(0, sep).toUpperCase();
            }


            char first = info.name.charAt(0);
            Log.i(TAG, "info.name = " + info.name + " first char = " + first);
            if(first == 'D') {
                Log.i(TAG, " digital device detected " + info.name);
                item.put("icon", R.drawable.ic_digital_device);
            } else {
                if (info.isAvailable)
                    item.put("icon", R.drawable.ic_buddy_online);
                else
                    item.put("icon", R.drawable.ic_buddy_offline);
            }

            if(info.name != null) {
                item.put("name", info.name);
            } else
                item.put("name", info.jid);
            mBuddyDataList.add(item);
        }
        bBuddyData = true;
    }

    public List<Map<String, Object>> getBuddyData() {
        Log.i(TAG, " getBuddyData bLogin = " + bLogin + " size = " + mBuddyDataList.size());
        //if(bBuddyData==false)
        getBuddyList();
        if(bLogin) {
            return mBuddyDataList;
        }
        return null;
    }

    public String getMyJid() {
        return mMyJid;
    }

    public boolean addBuddybyJid(String jid) {
        if(mConn.isConnected() == true) {
            try {
                Roster r = mConn.getRoster();
                r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                r.createEntry(jid+"@wuruxu.com", jid, null);
                Presence subscribe = new Presence(Presence.Type.subscribe);
                subscribe.setTo(jid+"@wuruxu.com");

                mConn.sendPacket(subscribe);
                return true;
            } catch (XMPPException e) {
                Log.e(TAG, "add Buddy failure " + e.getMessage());
            }
        }
        return false;
    }

    public void doInit() {
        bBuddyData = false;
        mBuddyDataList.clear();
        mBuddyInfo.clear();
/*        try {
            mRECollection.clear();
        } catch (UnsupportedOperationException e) {
            Log.i(TAG, "mRECollection.clear not needed");
        }*/

        if(mConn.isConnected()) {
            try {
                mConn.disconnect();
                mConn.connect();
            }catch (XMPPException e) {
                Log.i(TAG, "doInit failure");
            }
        }
    }

    public boolean doLogin(String user, String passwd, ChatManagerListener listener) {
        try {
            if(mConn.isConnected() == false) {
                mConn.connect();
            }

            mConn.login(user, passwd, "Smack");
            bLogin = true;
            if(listener != null) {
                ChatManager cm = mConn.getChatManager();
                cm.addChatListener(listener);
            }
            mMyJid = user;
            return true;
        } catch (XMPPException e ) {
            Log.e(TAG, "doLogin XMPPException " + e.getMessage());
        } catch (IllegalStateException e) {
            if( e.getMessage().equals("Already logged in to server.")) {
                //Log.e(TAG, "already logged in to server.");
                bLogin = true;
                mMyJid = user;
                return  true;
            }
        }
        return false;
    }

    public boolean setChatManagerListener(ChatManagerListener listener) {
        if(listener != null) {
            ChatManager cm = mConn.getChatManager();
            cm.addChatListener(listener);
            return true;
        }

        return false;
    }

    public boolean doRegister(String username, String passwd) {
        try {
            AccountManager am = mConn.getAccountManager();
            Map<String, String> attr = new HashMap<String, String>();
            attr.put("username", username);
            attr.put("password", passwd);
            attr.put("email", username + "@wuruxu.com");

            am.createAccount(username, passwd, attr);
            return true;

        } catch (XMPPException e) {
            Log.e(TAG, "doRegister " + username + " " + e.getMessage());
        }
        return false;
    }

    public boolean doDelete() {
        if(bLogin) {
            try {
                AccountManager am = mConn.getAccountManager();
                am.deleteAccount();
            } catch (XMPPException e) {
                Log.e(TAG, "doDelete " + e.getMessage());
            }

            return true;
        }
        return false;
    }

/*    public void doChat(String to, ChatManagerListener MsgListener) {
        if(bLogin) {
            ChatManager cm = mConn.getChatManager();
            //WeakReference wr = new WeakReference(cm);
            cm.addChatListener(MsgListener);
            mChatMap.put(to, cm);

        }
    }*/

    public boolean doSend(String to, String msg) {
        if(bLogin) {
            try {
                ChatManager cm = mConn.getChatManager();
                Chat chat = cm.createChat(to+"@wuruxu.com/Smack", null);
                chat.sendMessage(msg);
            } catch(XMPPException e) {
                Log.e(TAG, "doSend " + e.getMessage());
            }
            return true;
        }

        return false;
    }
}
