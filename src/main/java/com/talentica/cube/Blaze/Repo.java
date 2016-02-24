package com.talentica.cube.Blaze;

import com.bigdata.rdf.sail.webapp.client.ConnectOptions;
import com.bigdata.rdf.sail.webapp.client.JettyResponseListener;
import com.bigdata.rdf.sail.webapp.client.RemoteRepository;
import com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager;

/**
 * Created by aravindp on 22/2/16.
 */
public class Repo {

    private String endPoint = "http://localhost:9999/bigdata";
    private RemoteRepositoryManager repoManager;

    public Repo(){
        repoManager = new RemoteRepositoryManager(this.endPoint, false);
    }

    public Repo(String endPoint){
        this.endPoint = endPoint;
        repoManager = new RemoteRepositoryManager(this.endPoint, false);
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public RemoteRepository getInstance(String namespace){
        if(repoManager == null){
            repoManager = new RemoteRepositoryManager(this.endPoint, false);
        }
        RemoteRepository repoNameSpace = repoManager.getRepositoryForNamespace(namespace);
        return repoNameSpace;
    }

    public JettyResponseListener getStatus() throws Exception {
        ConnectOptions opts = getConnectOptions("GET",this.endPoint+"/status");
        return repoManager.doConnect(opts);
    }

    public ConnectOptions getConnectOptions(String reqType,String serviceUrl){
        ConnectOptions opts = new ConnectOptions(serviceUrl);
        opts.method = reqType;
        return opts;
    }

    public void close() throws Exception {
        if(repoManager != null){
            repoManager.close();
        }
    }


}
