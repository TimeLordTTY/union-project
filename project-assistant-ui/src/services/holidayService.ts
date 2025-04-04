// src/services/holidayService.ts
import axios from "axios";

export interface HolidayDetail {
  name: string;
  date: string;
  isOffDay: boolean;
}

export interface HolidayInfo {
  [date: string]: HolidayDetail;
}

export async function fetchHolidays(
  year: number
): Promise<Record<string, HolidayInfo>> {
  const response = await axios.get(
    `https://timor.tech/api/holiday/year/${year}`
  );
  const data = (await fetchHolidays(year)) as { holiday: HolidayInfo };

  const holidays: Record<string, HolidayInfo> = {};
  Object.entries(data).forEach(([date, value]: any) => {
    holidays[date] = {
      date,
      name: value.name,
      isOffDay: value.holiday || false,
    };
  });

  return holidays;
}
