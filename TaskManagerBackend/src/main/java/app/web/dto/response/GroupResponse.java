package app.web.dto.response;

import app.domain.Group;

public record GroupResponse(int id, String title, int creatorId) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(group.getId(), group.getTitle(), group.getCreatorId());
    }
}
