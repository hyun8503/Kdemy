package com.TeamProject.Kdemy.service.chat;

import java.util.List;

import com.TeamProject.Kdemy.model.chat.dto.ChatRoomDTO;

public interface ChatRoomService {

	public void createRoom(ChatRoomDTO dto) throws Exception;

	public List<ChatRoomDTO> chatRoomListAll() throws Exception;

	public ChatRoomDTO chatRoomView(int chatroom_id)throws Exception;
}
