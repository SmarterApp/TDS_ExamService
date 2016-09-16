package tds.exam.services;

import java.util.Optional;
import java.util.UUID;

import tds.session.Session;

public interface SessionService {
    Optional<Session> getSession(UUID sessionId);
}
