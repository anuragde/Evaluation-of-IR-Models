package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.TreeMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.renderscript.Sampler;
import android.telephony.TelephonyManager;
import android.util.Log;

import static android.content.ContentValues.TAG;
import static java.lang.Float.parseFloat;

class Node{
    private static final long serialVersionUID = 5950169519310163575L;
    SimpleDhtProvider sdh = new SimpleDhtProvider();
    String node_id = "";
    String hashedId;
    Node succ;
    Node pred;
    public Node(String node_id,String hashedId){
        this.node_id = node_id;
        this.hashedId = hashedId;
    }
    public Node lookUp(String id) {
        if(pred==null && succ ==null)
            return this;
        try {
            if (id.compareTo(pred.hashedId)>0 && id.compareTo(hashedId)<=0)
                return this;
            else
                return succ.lookUp(id);
        }
        catch (Exception ex) {
            Log.e("lookup error","lookup error");
        }
        return null;
    }
}

public class SimpleDhtProvider extends ContentProvider {
    SimpleDhtActivity sda = new SimpleDhtActivity();
    Node myNode;
    static final int SERVER_PORT = 10000;
    int clientID=-1;
    TreeMap tv = new TreeMap<String,Node>();
    public ArrayList<Node> nodeList= new ArrayList<Node>();

    ArrayList<Node> getNodeList(){
       return  nodeList;
    }

    void setNodeList(ArrayList<Node> nodeList){
        this.nodeList = nodeList;
    }

    Node getMyNode(){
        return myNode;
    }
    void setTreeMap(TreeMap<String,Node> tv){
        this.tv = tv;
    }

    TreeMap getTreeMap(){
        return tv;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        String fileName= selection;
        try {
            getContext().deleteFile(fileName);
        }
        catch(Exception e){
            Log.e("Exception Thrown","Exception Thrown");
        }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
       ClientTask ct = new ClientTask();

        // TODO Auto-generated method stub
        Log.v("insert", values.toString());
        String filename = values.getAsString("key");
        String string = values.getAsString("value");
        String hashedKey="";
        Node responsibleNode;
        try{
            hashedKey = genHash(filename);
        }
        catch(Exception e){
        }
     responsibleNode = myNode.lookUp(hashedKey);

    if ((myNode.node_id).equals(responsibleNode.node_id)) {
            Log.v("Created " + hashedKey, "with value " + string + "Before hash " + filename);
            FileOutputStream outputStream;
            try {//Context.MODE_PRIVATE
                outputStream = getContext().openFileOutput(hashedKey, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                Log.e("Exception Thrown", "Exception Thrown");
                Log.e(TAG, "File write failed");
            }
        }
       else
            ct.sendToResponsibleNode(uri,values,responsibleNode.node_id);
        return uri;
    }

    public boolean onCreate() {

        String portNo = "5554";//sda.getPort();
        String hashedPort="abc";
        try{
            hashedPort = genHash(portNo);
        }
        catch(Exception exception){
            Log.v("PortNumber: "+portNo,hashedPort);
            Log.e("onCreate()","Node creation Exception");
        }
        myNode = new Node(portNo,hashedPort);
        nodeList.add(myNode);
        tv.put(hashedPort,myNode);
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e("Exception Thrown", "Exception Thrown");
            Log.e(TAG, "Can't create a ServerSocket");
            return false;
        }
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key","value"});
        String filename = selection;
        String line="";
        try {
            Log.v("File input stream",filename);
            FileInputStream in = getContext().openFileInput(filename);
          /*  Log.e(TAG, "File inputStreamReader.");*/
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            line = bufferedReader.readLine();
            sb.append(line);
            in.close();
        }
        catch(Exception e) {
            Log.e("Exception Thrown","Exception Thrown");
            Log.e(TAG, "File read failed...");
            //   e.printStackTrace();
        }

        MatrixCursor.RowBuilder builder = matrixCursor.newRow();
        builder.add("key",selection);
        builder.add("value",line);

        matrixCursor.setNotificationUri(getContext().getContentResolver(),uri);
        //Log.e("query", line);
        return matrixCursor;
        //Log.v("query", selection);
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
class ServerTask extends AsyncTask<ServerSocket, String, Void> {
    //Should answer lookup queries (if not found forward the stack to next node)
// handle join request //done
// handle repartitioning (send out the unwanted data and prev node id details)
// receive data to store in local hash table //done
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    SimpleDhtProvider sdp = new SimpleDhtProvider();

    private void joinRequest(Node newNode) {
        ArrayList<Node> nodeList = sdp.getNodeList();
        nodeList.add(newNode);
        //broad cast the new node list to every other node
        sdp.setNodeList(nodeList);
        sendUpdatedList();
    }

    void sendUpdatedList() {
        try {
            String remotePort[] = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};
            ObjectOutputStream outputStream ;
            for (int i = 0; i < 5; i++) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort[i]));
                    outputStream = new ObjectOutputStream(socket.getOutputStream());
                //send tree map
                outputStream.writeObject(sdp.getTreeMap());
            }
        }
        catch (Exception ex) {
            Log.e("sendUpdatedList","sendUpdatedList");
        }
    }

        @Override
        protected Void doInBackground (ServerSocket...sockets){
            ServerSocket serverSocket = sockets[0];
            while (true) {
                try {
                    Socket serverS = serverSocket.accept();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(serverS.getInputStream()));
                    String receivedMessage = bufferedReader.readLine();
                    //DataOutputStream out = new DataOutputStream(serverS.getOutputStream());
                    // Log.v("------------","-----------");
                    //Log.v("ST--doInBackground", "Received new msg as " + receivedMessage);
                    // String delim ="aDel";
                    // String messageType="",message = "",finalSeq="" ;
                    ObjectInputStream inStream = new ObjectInputStream(serverS.getInputStream());
                    Node receivedNode = null;

                    if (receivedMessage != null && !receivedMessage.isEmpty()) {
                        if (receivedMessage.equals("joinRequest")) {
                            try {
                                receivedNode = (Node) inStream.readObject();
                            } catch (Exception ex) {
                                Log.e("doInBackground", "Class Not found");
                            }
                            joinRequest(receivedNode);
                            sendUpdatedList();
                        }
                        if(receivedMessage.equals("insertRequest")){
                            ContentValues receivedValues = new ContentValues();
                            Uri uri ;
                            try {
                                uri = (Uri) inStream.readObject();
                                receivedValues = (ContentValues) inStream.readObject();
                                //Uri mNewUri;
                               //mNewUri = getContentResolver().insert(uri,receivedValues);
                            } catch (Exception ex) {
                                Log.e("doInBackground","Class Not found");
                            }
                        }
                    }
//        String tokens[] = receivedMessage.split(delim);
//        messageType = tokens[0];
//        message = tokens[1];
//        int recvFrom = -1;
//        if(Integer.parseInt(tokens[3])!=-1)
//        skipServerID1 = Integer.parseInt(tokens[3]);
//
//        if (messageType.equals("sendProp")) {
//        recvFrom = Integer.parseInt(tokens[2]);

//        Log.v("ST--doInBackground", "Received message type as: " + messageType + "& msg as " + message);
//        if (!message.isEmpty()) {
//
//        sendProposedSeqNum(out, message, recvFrom);
//        printDataStructures(messageTag,seqTag,seqTagRev);
//        }
//        } else if (messageType.equals("sendFinalSeq")) {
//        finalSeq = tokens[2];
//        getSeqNum(finalSeq, message);
        // publishProgress(message);
//        deliverMessages(message,skipServerID1);
//        }
//        }
//        out.flush();
//        out.close();
//        bufferedReader.close();
                } catch (IOException e) {
                    Log.e("Exception Thrown", "Exception Thrown");
                    Log.e("Server Task", "Server IO Exception" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    protected void onProgressUpdate(String... strings) {
        String strReceived = strings[0].trim();
        //TextView remoteTextView = (TextView) findViewById(R.id.textView1);
        //remoteTextView.append(strReceived + "\t\n" + "\n");
        //Log.v("ST--Publish Progress ", "Publish Progress-1");
        //Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
        //Log.v("ST--Publish Progress ", "Publish Progress-2");
        ContentValues keyValueToInsert = new ContentValues();
       // keyValueToInsert.put("key", Integer.toString(counter));
        keyValueToInsert.put("value", strReceived);
       // getContentResolver().insert(uri, keyValueToInsert);
        //insert(uri, keyValueToInsert);
        //Log.v("ST--Publish Progress ", "Publish Progress End");
        //counter++;
        //Log.v("------------", "--------------");
    }
}



class ClientTask extends AsyncTask<String, Void, Void> {
    static final String REMOTE_PORT0 = "11108";
    SimpleDhtProvider sdh = new SimpleDhtProvider();
    SimpleDhtActivity sda1 = new SimpleDhtActivity();

    void sendJoinRequest(ObjectOutputStream out) {
        try {
            String sendProp ="joinRequest";
            out.writeBytes(sendProp+"\n");
            out.writeObject(sdh.getMyNode());
            out.flush();
            Log.v("sendJoinRequest" ,"sendJoinRequest");
        } catch (IOException io) {
            Log.e("Exception Thrown","Exception Thrown");
        }
    }

    void sendToResponsibleNode(Uri uri, ContentValues contentValues, String node_id) {
        try {
            OutputStream outToServer;
            ObjectOutputStream out;
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node_id)* 2);
            outToServer = socket.getOutputStream();
            out = new ObjectOutputStream(outToServer);
            String sendReq ="insertRequest";
            out.writeBytes(sendReq+"\n");
            out.writeObject(uri);
            out.writeObject(contentValues);
            out.flush();
        }
        catch(Exception ex){
            Log.e("sendToResponsibleNode","sendToResponsibleNode");
        }
    }


    @Override
    protected Void doInBackground(String... msgs) {
        try {
            OutputStream outToServer;
//            InputStream inToClient;
            ObjectOutputStream out;
            BufferedReader in;
            String port = sda1.getPort();
            if(!port.equals(REMOTE_PORT0)) {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT0));
                outToServer = socket.getOutputStream();
                out = new ObjectOutputStream(outToServer);
                //inToClient = socket.getInputStream();
                //in = new BufferedReader(new InputStreamReader(inToClient));
                sendJoinRequest(out);
                //
                // in.readLine();
                out.close();
            }
            }
         catch (IOException e) {
            Log.e("Exception Thrown","Exception Thrown");
            Log.e(TAG, "ClientTask socket IOException");
            e.printStackTrace();
        }
      return null;
     }
}
