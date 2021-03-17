package server;

import interfaces.Player;
import interfaces.Registro;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GamerServer implements Registro{
    private int countPlayer = 0;

    public static void main(String[] args){
        System.setProperty("java.security.policy","C:/Users/susy_/IdeaProjects/ProyectoAlphaV1/src/server/server.policy");
        MulticastSocket s =null;
        InetAddress group = null;
        Socket st = null;

        if(System.getSecurityManager()==null){
            System.setSecurityManager(new SecurityManager());
        }

        try{
            //Desplegar servicio de registro RMI
            LocateRegistry.createRegistry(1099);
            String name = "Registro";
            GamerServer engine = new GamerServer();
            Registro stub = (Registro) UnicastRemoteObject.exportObject(engine,0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name,stub);
            System.out.println("Servicio desplegado!");

            //Crear grupo mulitcast
            group = InetAddress.getByName("228.5.6.7");
            s = new MulticastSocket(6789);
            s.joinGroup(group);

            //VARIABLES TCPSERVER
            int serverPort = 7896;
            ServerSocket listenSocket = new ServerSocket(serverPort);

            //Enviar cada 5s un nuevo monstruo
            while (true){
                String message= Integer.toString((int) (Math.random()*(10-1)+1));
                byte[] m = message.getBytes();

                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);
                s.send(messageOut);
                //System.out.println(message);
                boolean mensajeRecibido=false;
                long startTime = System.currentTimeMillis();
                //Thread.sleep(5000);
                //while((System.currentTimeMillis()-startTime)<5000) {//true modificar
                     System.out.println("En espera...");
                    Socket clientSocket = listenSocket.accept(); // Listens for a connection to be made to this socket and accepts it. The method blocks until a connection is made.
                    Connection c = new Connection(clientSocket);
                    c.start();
                //}
            }


        } catch (RemoteException | UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(s != null){
                try {
                    s.leaveGroup(group);
                } catch (IOException ex) {
                    Logger.getLogger(GamerServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                s.close();
            }
        }
    }




    @Override
    public Player registro(String nombre) throws RemoteException {
        countPlayer ++;
        return new Player(nombre, countPlayer);
    }
}

class Connection extends Thread {
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    public Connection (Socket aClientSocket) {
        try {
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out =new DataOutputStream(clientSocket.getOutputStream());
        } catch(IOException e)  {System.out.println("Conexión: "+e.getMessage());}
    }

    @Override
    public void run(){
        try {
            int idPlayer=0;
            //while(idPlayer != -1){
                idPlayer = in.readInt();
            System.out.println(idPlayer);
            //}
        }
        catch(EOFException e) {
            System.out.println("EOF:"+e.getMessage());
        }
        catch(IOException e) {
            System.out.println("IO:"+e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e){
                System.out.println(e);
            }
        }
    }
}

