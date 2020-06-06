import requests
import json
import math
import csv

coord_name = "buildingCoords.json"

coord_file = open(coord_name)

class coord:
    def __init__(self, name, lon, lat):
        self.buiding_name = name
        self.longitude = lon
        self.latitude = lat
    def __str__(self):
        return self.buiding_name + " " + str(self.longitude) + " " + str(self.latitude)

def haversine_distance(lat1, lon1, lat2, lon2):
    radius = 6370120
    lat_diff = math.radians(abs(lat1 - lat2))
    lon_diff = math.radians(abs(lon1 - lon2))
    a = math.pow(math.sin(lat_diff / 2), 2) + (math.cos(lat1) * math.cos(lat2) * math.pow(math.sin(lon_diff / 2), 2))
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    return radius * c

building_list = json.load(coord_file)
building_coords = []

for building in building_list:
    print(building['name'])
    formatted_name = building['name'].split("/")[0]
    formatted_name = formatted_name.split("(")[0]
    formatted_name = formatted_name.split(",")[0]
    formatted_name = formatted_name.strip()
    print(formatted_name)
    building_coords.append(coord(formatted_name, float(building['longitude']), float(building['latitude'])))

with open('src/buildingDistances.csv', 'w+') as csvfile:
    csvwriter = csv.writer(csvfile)
    building_list = []
    for i in range(0, len(building_coords)):
        building_list.append(building_coords[i].buiding_name)
    csvwriter.writerow(building_list)
    for i in range(0, len(building_coords)):
        distance_list = []
        for j in range(0, len(building_coords)):
            distance_list.append(haversine_distance(building_coords[i].latitude, building_coords[i].longitude, building_coords[j].latitude, building_coords[j].longitude))
        csvwriter.writerow(distance_list)
