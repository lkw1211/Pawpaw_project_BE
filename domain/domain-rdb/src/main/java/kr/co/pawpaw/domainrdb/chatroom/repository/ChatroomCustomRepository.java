package kr.co.pawpaw.domainrdb.chatroom.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pawpaw.domainrdb.chatroom.domain.QChat;
import kr.co.pawpaw.domainrdb.chatroom.domain.QChatroom;
import kr.co.pawpaw.domainrdb.chatroom.domain.QChatroomParticipant;
import kr.co.pawpaw.domainrdb.chatroom.dto.ChatroomDetailData;
import kr.co.pawpaw.domainrdb.chatroom.dto.ChatroomResponse;
import kr.co.pawpaw.domainrdb.storage.domain.QFile;
import kr.co.pawpaw.domainrdb.user.domain.QUser;
import kr.co.pawpaw.domainrdb.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatroomCustomRepository {
    private final JPAQueryFactory queryFactory;
    private static final QChatroom qChatroom = QChatroom.chatroom;
    private static final QChatroomParticipant myQChatroomParticipant = new QChatroomParticipant("myQChatroomParticipant");
    private static final QChatroomParticipant qChatroomParticipant = new QChatroomParticipant("qChatroomParticipant");
    private static final QChatroomParticipant qChatroomParticipantManager = new QChatroomParticipant("qChatroomParticipantManager");
    private static final QUser qUserManager = QUser.user;
    private static final QFile qFileCover = new QFile("qFileCover");
    private static final QFile qFileManager = new QFile("qFileManager");
    private static final QChat qChat = QChat.chat;

    public List<ChatroomDetailData> findAllByUserIdWithDetailData(final UserId userId) {
        return queryFactory.select(
                Projections.constructor(
                    ChatroomDetailData.class,
                    qChatroom.id,
                    qChatroom.name,
                    qChatroom.description,
                    qFileCover.fileUrl,
                    qChat.createdDate.max(),
                    qChatroom.hashTagList,
                    qUserManager.nickname,
                    qFileManager.fileUrl,
                    qChatroomParticipant.countDistinct(),
                    Expressions.constant(false),
                    Expressions.constant(false)
                )
            )
            .from(myQChatroomParticipant)
            .innerJoin(myQChatroomParticipant.chatroom, qChatroom)
            .leftJoin(qChatroom.coverFile, qFileCover)
            .innerJoin(qChatroom.manager, qChatroomParticipantManager)
            .innerJoin(qChatroomParticipantManager.user, qUserManager)
            .leftJoin(qUserManager.userImage, qFileManager)
            .leftJoin(qChatroomParticipant).on(qChatroom.eq(qChatroomParticipant.chatroom))
            .leftJoin(qChat).on(qChatroom.eq(qChat.chatroom))
            .where(myQChatroomParticipant.user.userId.eq(Expressions.constant(userId)))
            .groupBy(
                qChatroom.id,
                qChatroom.name,
                qChatroom.description,
                qFileCover.fileUrl,
                qChatroom.hashTagList,
                qUserManager.nickname,
                qFileManager.fileUrl
            )
            .fetch();
    }

    public List<ChatroomResponse> findAccessibleNewChatroomByUserId(final UserId userId) {
        return queryFactory.select(
                Projections.constructor(
                    ChatroomResponse.class,
                    qChatroom.id,
                    qChatroom.name,
                    qChatroom.description,
                    qChatroom.hashTagList,
                    qUserManager.nickname,
                    qFileManager.fileUrl,
                    qChatroomParticipant.count()
                )
            )
            .from(qChatroom)
            .innerJoin(qChatroom.manager, qChatroomParticipantManager)
            .innerJoin(qChatroomParticipantManager.user, qUserManager)
            .innerJoin(qUserManager.userImage, qFileManager)
            .leftJoin(qChatroom.chatroomParticipants, qChatroomParticipant)
            .where(qChatroom.id.notIn(
                JPAExpressions.select(qChatroomParticipant.chatroom.id)
                    .from(qChatroomParticipant)
                    .where(qChatroomParticipant.user.userId.eq(userId)))
                .and(qChatroom.searchable.isTrue()))
            .groupBy(
                qChatroom.id,
                qChatroom.name,
                qChatroom.description,
                qChatroom.hashTagList,
                qUserManager.nickname,
                qFileManager.fileUrl
            )
            .orderBy(new OrderSpecifier(Order.ASC, qChatroom.createdDate))
            .offset(0)
            .limit(10)
            .fetch();
    }
}