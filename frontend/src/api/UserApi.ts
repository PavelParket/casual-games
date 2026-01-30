import { client } from "./AxiosConfig";
import type { UpdateUserRequest, User } from "../models/User";

const USER_SERVICE_URL = 'http://localhost:8083';

export const UserAPI = {
  findByGuid: (guid: string) => client.get<User>(`${USER_SERVICE_URL}/users/guid=${guid}`),

  updateByGuid: (guid: string, data: UpdateUserRequest) =>
    client.put<User>(`${USER_SERVICE_URL}/users/guid=${guid}`, data),
};