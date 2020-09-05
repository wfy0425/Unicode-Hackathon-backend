package com.professionalbugcoding.unicodesc.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.professionalbugcoding.unicodesc.bean.RoomBean;
import com.professionalbugcoding.unicodesc.bean.UserBean;
import com.professionalbugcoding.unicodesc.mapper.RoomMapper;
import com.professionalbugcoding.unicodesc.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private final UserMapper userMapper = null;

    @Autowired
    private final RoomMapper roomMapper = null;

    public UserBean getUserByEmail(String email) {
        return userMapper.selectOne(new QueryWrapper<UserBean>().eq("email", email));

    }

    public int addUser(String email, String password, String name, String myTeams, String joinedTeams, int sex) {

        UserBean userBean = new UserBean();
        userBean.setName(name);
        userBean.setEmail(email);
        userBean.setPassword(password);
        userBean.setMyTeams(myTeams);
        userBean.setJoinedTeams(joinedTeams);
        userBean.setSex(sex);

        return userMapper.insert(userBean);

    }


    public List<UserBean> getUserList() {
        return userMapper.selectList(null);

    }

    public UserBean getUserInfoByUserId(int id) {
        return userMapper.selectById(id);

    }


    public List<RoomBean> getRoomList() {
        return roomMapper.selectList(null);

    }

    public RoomBean getRoomInfoByRoomId(int hostId) {
        return roomMapper.selectById(hostId);

    }

    public List<RoomBean> getRoomInfoByHostId(int hostId) {
        UserBean host = userMapper.selectById(hostId);
        System.out.println(host.getMyTeams());
        String[] roomsId = host.getMyTeams().split(",");
        List<Integer> roomsIdList = new ArrayList<Integer>();
        for (String s : roomsId) {
            roomsIdList.add(Integer.parseInt(s));
        }
        return roomMapper.selectBatchIds(roomsIdList);

    }

    public int updateUserByUserId(int id, UserBean userBean) {
        userBean.setId(id);
        return userMapper.updateById(userBean);

    }

    public RoomBean createRoom(String date, String time, String gameName, int maxNumber, int hostId) {
        RoomBean roomBean = new RoomBean();
        roomBean.setDateTime(date + time);
        roomBean.setGameName(gameName);
        roomBean.setMaxNumber(maxNumber);
        roomBean.setHostId(hostId);
        roomMapper.insert(roomBean);
        UserBean userBean = this.getUserInfoByUserId(hostId);
        userBean.setJoinedTeams(userBean.getJoinedTeams() + "," + roomBean.getId());
        updateUserByUserId(hostId, userBean);
        return roomBean;
    }

    public int updateRoomByRoomId(int id, RoomBean roomBean) {
        roomBean.setId(id);
        return roomMapper.updateById(roomBean);

    }

    public int deleteRoomByRoomId(int roomId) {
        RoomBean room = this.getRoomInfoByRoomId(roomId);
        String[] members = room.getMembersId().split(",");
        for (String memberStr : members) {
            int memberId = Integer.parseInt(memberStr);
            UserBean userBean = this.getUserInfoByUserId(memberId);
            String[] joinedTeams = userBean.getJoinedTeams().split(",");
            StringBuilder sb = new StringBuilder();
            for (String s : joinedTeams) {
                if (roomId != Integer.parseInt(s))
                    sb.append(s).append(",");
            }
            UserBean updateInfo = new UserBean();
            updateInfo.setJoinedTeams(sb.toString());
            this.updateUserByUserId(userBean.getId(), updateInfo);
        }

        UserBean hostUserBean = this.getUserInfoByUserId(room.getHostId());
        String[] hostedTeams = hostUserBean.getMyTeams().split(",");
        StringBuilder sb1 = new StringBuilder();
        for (String s : hostedTeams) {
            if (roomId != Integer.parseInt(s))
                sb1.append(s).append(",");
        }
        UserBean updateHostInfo = new UserBean();
        updateHostInfo.setMyTeams(sb1.toString());
        this.updateUserByUserId(hostUserBean.getId(), updateHostInfo);
        return roomMapper.deleteById(roomId);
    }

//    public  List<RoomBean> getRoomInfoByHostEmail(String hostEmail) {
//        return this.getRoomInfoByHostId(this.getUserByEmail(hostEmail).getId());
//
//    }

    public int joinTeam(int roomId, int userId) {
        RoomBean roomBean = this.getRoomInfoByRoomId(roomId);
        roomBean.setMembersId(roomBean.getMembersId() + userId);
        UserBean userBean = this.getUserInfoByUserId(userId);
        userBean.setJoinedTeams(userBean.getJoinedTeams() + roomId);
        this.updateRoomByRoomId(roomId, roomBean);
        return this.updateUserByUserId(userId, userBean);
    }

    public int removeUserFromJoinedTeam(int roomId, int userId) {
        RoomBean room = this.getRoomInfoByRoomId(roomId);
        String[] members = room.getMembersId().split(",");
        for (String memberStr : members) {
            StringBuilder sb = new StringBuilder();
            if (userId != Integer.parseInt(memberStr))
                sb.append(memberStr).append(",");
            RoomBean updateRoomInfo = new RoomBean();
            updateRoomInfo.setMembersId(sb.toString());
            this.updateRoomByRoomId(roomId, updateRoomInfo);
        }

        UserBean userBean = this.getUserInfoByUserId(userId);
        String[] joinedTeam = userBean.getJoinedTeams().split(",");
        StringBuilder sb1 = new StringBuilder();
        for (String s : joinedTeam) {
            if (roomId != Integer.parseInt(s))
                sb1.append(s).append(",");
        }
        UserBean updateUserBean = new UserBean();
        updateUserBean.setJoinedTeams(sb1.toString());
        return this.updateUserByUserId(userId, updateUserBean);
    }
}
