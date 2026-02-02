import type { UpdateUserRequest, User } from "../models/User";
import axios from "axios";

const USER_SERVICE_URL = 'http://localhost:8083';

export const UserAPI = {
  findByGuid: (guid: string) => axios.get<User>(`${USER_SERVICE_URL}/users/guid=${guid}`),

  updateByGuid: (guid: string, data: UpdateUserRequest) => axios.put<User>(`${USER_SERVICE_URL}/users/guid=${guid}`, data),
};
