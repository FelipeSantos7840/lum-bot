package org.lumbot.collector;

import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.lumbot.collector.collectorservice.FileCollectorRSS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class CollectorRSS {
    private TypeRSS type;
    public List<DataRSS> dataList = new ArrayList<>();

    public CollectorRSS(TypeRSS type) {
        this.type = type;
    }

    public TypeRSS getType() {
        return type;
    }

    public static SyndFeed getFeed(String url){
        try{
            URL feedUrl = new URL(url);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            return feed;
        } catch (MalformedURLException e){
            System.out.println("Link Quebrado: " + e.getLocalizedMessage());
        } catch (IOException | FeedException e){
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        throw new IllegalStateException("Erro ao Gerar Feed!");
    }

    public boolean collectAndProcessRSS(String url){

        SyndFeed feed = CollectorRSS.getFeed(url);

        LocalDateTime lastPublishTime = LocalDateTime.ofInstant(feed.getPublishedDate().toInstant(), ZoneId.of("America/Sao_Paulo"));
        boolean validate = verifyModification(lastPublishTime);

        if(validate){
            List<SyndEntry> entryList = feed.getEntries();
            List<DataRSS> datas = new ArrayList<>();
            LocalDateTime pubTime;
            SyndEnclosure sde;
            for(SyndEntry entry : entryList){
                pubTime = LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.of("America/Sao_Paulo"));
                sde = entry.getEnclosures().get(0);
                datas.add(new DataRSS(entry.getTitle(),pubTime,sde.getUrl(),entry.getLink()));
            }
            List<DataRSS> list = new ArrayList<>();
            String lastData = FileCollectorRSS.getLastDataReceived(type);
            if(lastData != null){
                boolean entry = true;
                int x=0;
                for(DataRSS newData : datas){
                    if(newData.toString().equals(lastData)){
                        break;
                    }
                    if(entry){
                        list.add(newData);
                    }
                    x++;
                }
                FileCollectorRSS.setLastDataReceived(type,list.get(0));
                this.dataList = list;
            } else {
                FileCollectorRSS.setLastDataReceived(type,datas.get(0));
                this.dataList = datas;
            }
            return true;
        }
        return false;
    }

    public boolean verifyModification(LocalDateTime lastPublishTime){
        LocalDateTime regPubTime = FileCollectorRSS.getLastPubSend(type);

        if(regPubTime.isBefore(lastPublishTime)){
            FileCollectorRSS.setLastPubSend(type,lastPublishTime);
            return true;
        }
        return false;
    }
}
