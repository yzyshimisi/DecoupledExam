import { request } from '../axios';

interface RegisterData {
  username: string;
  password: string;
  realName: string;
  phone: string;
  email: string;
}

const registerAPI = async (data: RegisterData) => {
  return request("/api/user/register", {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    data: data
  })
}

export default registerAPI;
