import type { UpdateUserRequest, User } from "../models/User";
import axios from "axios";
import { USER_SERVICE_URL } from "./ApiDictionary";

export const UserAPI = {
  findByGuid: (guid: string) => axios.get<User>(`${USER_SERVICE_URL}/users/guid=${guid}`),

  updateByGuid: (guid: string, data: UpdateUserRequest) => axios.put<User>(`${USER_SERVICE_URL}/users/guid=${guid}`, data),

  getBalance: (guid: string) => axios.get<number>(`${USER_SERVICE_URL}/users/balance/${guid}`),
};
