package org.labex.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

@SuppressWarnings("unused")
class LabexConnection {
    static boolean initConnection(Context context) {
        if (!btAdapter.isEnabled())
            return false;

        Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();

        BluetoothDevice labexBTDevice = null;
        for (BluetoothDevice dev : bondedDevices)
            if (dev.getAddress().equals(context.getString(R.string.labexMAC))) {
                labexBTDevice = dev;
                break;
            }

        if (labexBTDevice == null)
            return false;

        ParcelUuid[] uuids = labexBTDevice.getUuids();
        labexSocket = null;

        try {
            labexSocket = labexBTDevice.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            labexSocket.connect();
        } catch (IOException e) {
            return false;
        }

        try {
            outputStream = labexSocket.getOutputStream();
            inputStream = labexSocket.getInputStream();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    // region VARIABLES
    static BluetoothAdapter btAdapter;
    private static OutputStream outputStream;
    private static InputStream inputStream;
    static BluetoothSocket labexSocket;
    // endregion

    static boolean closeConnection() {
        try {
            labexSocket.close();
            return true;
        } catch (IOException e) {
            //Toast.makeText(getApplicationContext(), "Labex couldn't be disconnected", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    static boolean labexConnected() {
        return btAdapter != null && btAdapter.isEnabled() && labexSocket != null && labexSocket.isConnected();
    }

    static boolean sendByte(byte data) {
        boolean res;

        try {
            outputStream.write(data);
            res = true;
        } catch (IOException e) {
            res = false;
        }

        Log.e("BYTE SEND", String.format("[DATA: %d]\t[STATUS: %s]", data, res ? "SENT" : "FAILED"));
        return res;
    }

    static boolean sendAction(byte action, byte data, boolean log) {
        boolean res;

        try {
            outputStream.write(action);
            outputStream.write(data);
            res = true;
        } catch (IOException e) {
            res = false;
        }

        if (log)
            Log.e("ACTION SEND", String.format("[ACTION: %d]\t[DATA: %d]\t[STATUS: %s]", action, data, res ? "SENT" : "FAILED"));
        return res;
    }

    static byte readByte(boolean log) {
        boolean res;
        byte ret;

        try {
            ret = (byte) inputStream.read();
            res = true;
        } catch (IOException e) {
            ret = -1;
            res = false;
        }

        if (log)
            Log.e("BYTE READ", String.format("[DATA: %d]\t[STATUS: %s]", ret, res ? "READ" : "FAILED"));
        return ret;
    }
}
