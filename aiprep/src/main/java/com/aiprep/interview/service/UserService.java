package com.aiprep.interview.service;

import com.aiprep.interview.dto.*;
import com.aiprep.interview.entity.User;

public interface UserService {
    AuthResponse register(RegisterDTO request);
    AuthResponse login(LoginDTO request);
    void forgotPassword(ForgotPasswordDTO request);
    void resetPassword(ResetPasswordDTO request);
    User getUserByEmail(String email);
}
