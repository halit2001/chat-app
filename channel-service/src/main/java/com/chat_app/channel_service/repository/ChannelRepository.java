package com.chat_app.channel_service.repository;

import com.chat_app.channel_service.model.Channel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends MongoRepository<Channel, String> {
}
