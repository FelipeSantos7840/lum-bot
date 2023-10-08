package org.example.application;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.example.application.servicebot.GenerateBotService;
import org.example.service.ConfigCommandsService;

public class Main {
    public static void main(String[] args){
        JDABuilder lumBot = GenerateBotService.provideLum();
        if(lumBot != null){
            lumBot.setActivity(Activity.playing("Mortal Kombat 1"));
            lumBot.setStatus(OnlineStatus.ONLINE);
            JDA jda = lumBot.build();
            try{
                jda.awaitReady();
                ConfigCommandsService.updateCommands(jda.getGuilds().get(0));
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Não Gerado Bot Lum!");
        }
    }
}
