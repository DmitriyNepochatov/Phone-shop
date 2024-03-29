package ua.com.webservice.mapper;

import org.springframework.data.domain.Pageable;
import ua.com.webservice.model.dto.CommentForPhone;
import ua.com.webservice.model.dto.PhoneForMainView;
import ua.com.webservice.repository.comment.CommentRepository;
import java.util.List;

public final class CommentMapper {
    private CommentMapper() {
    }

    public static List<CommentForPhone> mapCommentToCommentForPhone(CommentRepository commentRepository, List<CommentForPhone> commentForPhoneList, PhoneForMainView phoneForMainView, Pageable pageable) {
        commentRepository.findAllCommentsForPhone(phoneForMainView.getBrand(), phoneForMainView.getName(), phoneForMainView.getSeries(),
                phoneForMainView.getAmountOfBuiltInMemory(), phoneForMainView.getAmountOfRam(), pageable).forEach(comment ->
                commentForPhoneList.add(new CommentForPhone(comment.getRegisteredUser().getLastName(), comment.getRegisteredUser().getFirstName(),
                        comment.getDescription())));

        return commentForPhoneList;
    }
}
